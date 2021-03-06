package controllers.gui;

import gov.nrel.util.Utility;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.ChartDbo;
import models.EntityUser;
import models.message.ChartMeta;
import models.message.ChartPageMeta;
import models.message.ChartVarMeta;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alvazan.play.NoSql;

import controllers.gui.auth.GuiSecure;
import controllers.gui.util.Chart;
import controllers.gui.util.ChartComparator;
import controllers.gui.util.ChartInfo;
import controllers.gui.util.ChartUtil;

import play.Play;
import play.mvc.Controller;
import play.mvc.With;
import play.templates.JavaExtensions;
import play.vfs.VirtualFile;

@With(GuiSecure.class)
public class MyChartsGeneric extends Controller {

	private static final Logger log = LoggerFactory.getLogger(MyChartsGeneric.class);

	private static ChartUtil chartUtil = new ChartUtil();
	
	public static void selectChart() {
		List<ChartInfo> charts = chartUtil.fetchCharts();
		String encoded = "start";
		render(charts, encoded);
	}

	public static void modifyChart(String chartId, String encoded) {
		List<ChartInfo> charts = chartUtil.fetchCharts();
		render("@selectChart", charts, chartId, encoded);
	}

	public static void postSelectedChart(String chartId, String encoded) {
		ChartInfo chart = ChartUtil.fetchChart(chartId);
		
		if(chart.isBuiltinChart()) {
			redirect(chart.getRoute());
		}
		
		chartVariables(chartId, 0, encoded);
	}

	public static void chartVariables(String chartId, int page, String encoded) {
		ChartInfo chart = ChartUtil.fetchChart(chartId);
		if(page < 0 || page >= chart.getChartMeta().getPages().size())
			notFound("this page is not found");
		ChartPageMeta pageMeta = chart.getChartMeta().getPages().get(page);
		if(!"start".equals(encoded)) {
			Map<String, String> variables = ChartUtil.decodeVariables(encoded);
			for(ChartVarMeta var : pageMeta.getVariables()) {
				String value = variables.get(var.getNameInJavascript());
				var.setValue(value);
			}
		}

		String subtitle = "("+(page+1) +" of "+chart.getChartMeta().getPages().size()+")";
		boolean isLastPage = false;
		if(page == chart.getChartMeta().getPages().size()-1)
			isLastPage = true;
		render(chart, pageMeta, chartId, page, encoded, isLastPage, subtitle);
	}

	public static void postVariables(String chartId, int page, String encoded) {
		ChartInfo chart = ChartUtil.fetchChart(chartId);

		Map<String, String[]> paramMap = params.all();
		Map<String, String> variablesMap = ChartUtil.decodeVariables(encoded);
		for(String key : paramMap.keySet()) {
			if(key.startsWith("chart.")) {
				String[] values = paramMap.get(key);
				String value = values[0];
				String javascriptKey = key.substring("chart.".length());
				if(javascriptKey.endsWith("url"))
					value = stripOffHost(value);
				variablesMap.put(javascriptKey, value);
			}
		}

		encoded = ChartUtil.encodeVariables(variablesMap);
		
		page = page+1;
		if(page >= chart.getChartMeta().getPages().size())
			drawChart(chartId, encoded);
		else
			chartVariables(chartId, page, encoded);
	}

	private static String stripOffHost(String value) {
		if(!value.startsWith("http"))
			return value;
		int index = value.indexOf("//");
		String val = value.substring(index+2);
		int nextIndex = val.indexOf("/");
		String relativeUrl = val.substring(nextIndex);
		return relativeUrl;
	}

	public static void drawChart(String chartId, String encoded) {
		EntityUser user = Utility.getCurrentUser(session);
		List<ChartDbo> charts = user.getCharts();
		//special case for showing remote pages
		Map<String, String> variables = ChartUtil.decodeVariables(encoded);
		
		ChartDbo chart = new ChartDbo();
		chart.setChartId(chartId);
		chart.setEncodedVariables(encoded);
		String title = chart.getTitle();
		String url = chart.getLargeChartUrl();
		render(charts, variables, url, title, chartId, encoded);
	}

	public static void largeChart(String chartId, String encoded) {
		ChartInfo info = ChartUtil.fetchChart(chartId);
		String c = info.getLargeChart();
		Map<String, String> variables = ChartUtil.decodeVariables(encoded);
		String chart = ChartUtil.replaceVariables(c, variables);
		render("@chartonly", info, chart, chartId, encoded);
	}

	public static void smallChart(String chartId, String encoded) {
		ChartInfo info = ChartUtil.fetchChart(chartId);
		String c = info.getSmallChart();
		Map<String, String> variables = ChartUtil.decodeVariables(encoded);
		String chart = ChartUtil.replaceVariables(c, variables);
		render("@chartonly", info, chart, chartId, encoded);
	}

	public static void addChartToDashboard(String chartId, String encoded) {
		//make sure chart exists...
		ChartUtil.fetchChart(chartId);
		ChartDbo chart = new ChartDbo();
		chart.setChartId(chartId);
		chart.setEncodedVariables(encoded);
		//he could create multiples of the same chart so just timestamp it as he would not
		//be fast enough to create ones with an id clash...
		chart.setId(chartId+System.currentTimeMillis());

		EntityUser user = Utility.getCurrentUser(session);
		List<ChartDbo> charts = user.getCharts();
		charts.add(chart);
		NoSql.em().put(user);
		NoSql.em().flush();
		
		Settings.charts();
	}
}
