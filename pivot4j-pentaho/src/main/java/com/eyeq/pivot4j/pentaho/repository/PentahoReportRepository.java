package com.eyeq.pivot4j.pentaho.repository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.NullArgumentException;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.RepositoryUtils;

import com.eyeq.pivot4j.analytics.repository.AbstractFileSystemRepository;
import com.eyeq.pivot4j.analytics.repository.ReportContent;
import com.eyeq.pivot4j.analytics.repository.ReportFile;

import edu.emory.mathcs.backport.java.util.Arrays;

public class PentahoReportRepository extends AbstractFileSystemRepository {

	private IPentahoSession session;

	private IUnifiedRepository repository;

	@PostConstruct
	protected void initialize() {
		this.session = PentahoSessionHolder.getSession();
		this.repository = PentahoSystem.get(IUnifiedRepository.class, session);
	}

	/**
	 * @return the session
	 */
	protected IPentahoSession getSession() {
		return session;
	}

	/**
	 * @return the repository
	 */
	protected IUnifiedRepository getRepository() {
		return repository;
	}

	/**
	 * @throws IOException
	 * @see com.eyeq.pivot4j.analytics.repository.ReportRepository#getRoot()
	 */
	@Override
	public PentahoReportFile getRoot() throws IOException {
		return getFile(ReportFile.SEPARATOR);
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.ReportRepository#exists(java.lang.String)
	 */
	@Override
	public boolean exists(String path) throws IOException {
		return getFile(path) != null;
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.ReportRepository#fileWithIdExists(java.lang.String)
	 */
	@Override
	public boolean fileWithIdExists(String id) throws IOException {
		return repository.getFileById(id) != null;
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.ReportRepository#getFile(java.lang.String)
	 */
	@Override
	public PentahoReportFile getFile(String path) throws IOException {
		if (path == null) {
			throw new NullArgumentException("path");
		}

		List<?> segments = Arrays.asList(path.split(ReportFile.SEPARATOR));

		List<PentahoReportFile> ancestors = new ArrayList<PentahoReportFile>(
				segments.size());

		ancestors.add(new PentahoReportFile(repository
				.getFile(ReportFile.SEPARATOR), null));

		StringBuilder builder = new StringBuilder();

		for (Object segment : segments) {
			String name = segment.toString();

			if (!name.isEmpty()) {
				builder.append(name);

				RepositoryFile file = repository.getFile(builder.toString());

				if (file == null) {
					return null;
				}

				ancestors.add(new PentahoReportFile(file, ancestors
						.get(ancestors.size() - 1)));
			}

			builder.append(ReportFile.SEPARATOR);
		}

		return ancestors.get(ancestors.size() - 1);
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.ReportRepository#getFileById(java.lang.String)
	 */
	@Override
	public PentahoReportFile getFileById(String id) throws IOException {
		RepositoryFile file = repository.getFileById(id);

		if (file == null) {
			return null;
		}

		return getFile(file.getPath());
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.ReportRepository#getFiles(com.eyeq.pivot4j.analytics.repository.ReportFile)
	 */
	@Override
	public List<ReportFile> getFiles(ReportFile parent) throws IOException {
		if (parent == null) {
			throw new NullArgumentException("parent");
		}

		List<ReportFile> files = new LinkedList<ReportFile>();

		if (parent.isDirectory()) {
			List<RepositoryFile> children = repository.getChildren(parent
					.getId());

			for (RepositoryFile file : children) {
				files.add(new PentahoReportFile(file,
						(PentahoReportFile) parent));
			}
		}

		return files;
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.ReportRepository#createFile(com.eyeq.pivot4j.analytics.repository.ReportFile,
	 *      java.lang.String,
	 *      com.eyeq.pivot4j.analytics.repository.ReportContent)
	 */
	@Override
	public ReportFile createFile(ReportFile parent, String name,
			ReportContent content) throws IOException, ConfigurationException {
		return createFile(parent, name, content, true);
	}

	/**
	 * @param parent
	 * @param name
	 * @param content
	 * @param overwrite
	 * @return
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public ReportFile createFile(ReportFile parent, String name,
			ReportContent content, boolean overwrite) throws IOException,
			ConfigurationException {
		if (parent == null) {
			throw new NullArgumentException("parent");
		}

		if (name == null) {
			throw new NullArgumentException("name");
		}

		if (content == null) {
			throw new NullArgumentException("content");
		}

		RepositoryUtils utils = new RepositoryUtils(repository);

		RepositoryFile file = utils.saveFile(parent.getPath()
				+ ReportFile.SEPARATOR + name, createReportData(content), true,
				overwrite, false, false, null);

		return new PentahoReportFile(file, (PentahoReportFile) parent);
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.ReportRepository#createDirectory(com.eyeq.pivot4j.analytics.repository.ReportFile,
	 *      java.lang.String)
	 */
	@Override
	public ReportFile createDirectory(ReportFile parent, String name)
			throws IOException {
		if (parent == null) {
			throw new NullArgumentException("parent");
		}

		if (name == null) {
			throw new NullArgumentException("name");
		}

		RepositoryFile directory = new RepositoryFile.Builder(name)
				.folder(true).build();
		directory = repository.createFolder(parent.getId(), directory, null);

		return new PentahoReportFile(directory, (PentahoReportFile) parent);
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.ReportRepository#renameFile(com.eyeq.pivot4j.analytics.repository.ReportFile,
	 *      java.lang.String)
	 */
	@Override
	public ReportFile renameFile(ReportFile file, String newName)
			throws IOException {
		if (file == null) {
			throw new NullArgumentException("file");
		}

		if (newName == null) {
			throw new NullArgumentException("newName");
		}

		String path = file.getParent().getPath() + ReportFile.SEPARATOR
				+ newName;

		repository.moveFile(file.getId(), path, null);

		return file;
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.ReportRepository#deleteFile(com.eyeq.pivot4j.analytics.repository.ReportFile)
	 */
	@Override
	public void deleteFile(ReportFile file) throws IOException {
		if (file == null) {
			throw new NullArgumentException("file");
		}

		repository.deleteFile(file.getId(), null);
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.ReportRepository#readContent(com.eyeq.pivot4j.analytics.repository.ReportFile)
	 */
	@Override
	public InputStream readContent(ReportFile file) throws IOException {
		if (file == null) {
			throw new NullArgumentException("file");
		}

		SimpleRepositoryFileData data = repository.getDataForRead(file.getId(),
				SimpleRepositoryFileData.class);

		return data.getInputStream();
	}

	/**
	 * @see com.eyeq.pivot4j.analytics.repository.ReportRepository#setReportContent(com.eyeq.pivot4j.analytics.repository.ReportFile,
	 *      com.eyeq.pivot4j.analytics.repository.ReportContent)
	 */
	@Override
	public void setReportContent(ReportFile file, ReportContent content)
			throws IOException, ConfigurationException {
		if (file == null) {
			throw new NullArgumentException("file");
		}

		if (content == null) {
			throw new NullArgumentException("content");
		}

		PentahoReportFile pentahoFile = (PentahoReportFile) file;

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		content.write(out);

		out.flush();
		out.close();

		repository.updateFile(pentahoFile.getFile(), createReportData(content),
				null);
	}

	/**
	 * @param content
	 * @return
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	protected IRepositoryFileData createReportData(ReportContent content)
			throws ConfigurationException, IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		content.write(out);

		out.flush();
		out.close();

		return new SimpleRepositoryFileData(new ByteArrayInputStream(
				out.toByteArray()), "UTF-8", "text/xml");
	}
}
