/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui;

import org.olap4j.Axis;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.state.Bookmarkable;
import com.eyeq.pivot4j.state.Configurable;
import com.eyeq.pivot4j.ui.aggregator.AggregatorFactory;
import com.eyeq.pivot4j.ui.aggregator.AggregatorPosition;

public interface PivotRenderer extends Configurable, Bookmarkable {

	void initialize();

	void render(PivotModel model);

	boolean getHideSpans();

	void setHideSpans(boolean hideSpans);

	boolean getShowParentMembers();

	void setShowParentMembers(boolean showParentMembers);

	boolean getShowDimensionTitle();

	void setShowDimensionTitle(boolean showDimensionTitle);

	PropertyCollector getPropertyCollector();

	void setPropertyCollector(PropertyCollector collector);

	AggregatorFactory getAggregatorFactory();

	String getAggregatorName(Axis axis, AggregatorPosition position);

	void setAggregatorName(Axis axis, AggregatorPosition position, String name);

	void swapAxes();
}