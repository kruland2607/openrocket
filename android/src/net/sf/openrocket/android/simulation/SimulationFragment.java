
package net.sf.openrocket.android.simulation;

import net.sf.openrocket.R;
import net.sf.openrocket.android.Application;
import net.sf.openrocket.document.OpenRocketDocument;

import org.achartengine.GraphicalView;
import org.achartengine.chart.XYChart;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

/**
 * An activity that encapsulates a graphical view of the chart.
 */
public class SimulationFragment extends Fragment implements SimulationSeriesDialog.OnConfirmListener {
	
	SimulationChart chart;

	ViewGroup container;
	
	/** The encapsulated graphical view. */
	private GraphicalView mView;
	/** The chart to be drawn. */
	private XYChart mChart;

	public static SimulationFragment newInstance( SimulationChart chart ) {
		SimulationFragment frag = new SimulationFragment();
		frag.chart = chart;
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setRetainInstance(false);
		setHasOptionsMenu(true);
		OpenRocketDocument rocketDocument = ((Application)getActivity().getApplication()).getRocketDocument();

		this.container = container;
		if (savedInstanceState != null ) {
			chart = (SimulationChart) savedInstanceState.getSerializable("chart");
		}
		mChart = chart.buildChart(rocketDocument);
		mView = new GraphicalView(container.getContext(), mChart);
		return mView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO without the clear it seems to create multiple copies
		// of the menu items on orientation change.
		menu.clear();
		inflater.inflate(R.menu.simulation_option_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId())
		{
		case R.id.simulation_select_series_menu_option:
			SimulationSeriesDialog seriesDialog = SimulationSeriesDialog.newInstance(chart);
			seriesDialog.show(getFragmentManager(), "AbraCadaver");
			seriesDialog.setOnConfirmListener(this);
			return true;
		case R.id.simulation_select_events_menu_option:
			SimulationEventsDialog eventsDialog = SimulationEventsDialog.newInstance(chart);
			eventsDialog.show(getFragmentManager(), "AbraCadaver");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onConfirm() {
		OpenRocketDocument rocketDocument = ((Application)getActivity().getApplication()).getRocketDocument();

		mChart = chart.buildChart(rocketDocument);
		ViewGroup parent = (ViewGroup) mView.getParent();
		parent.removeView(mView);
		mView = new GraphicalView(container.getContext(), mChart);
		parent.addView(mView);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("chart", chart);

	}
	
}