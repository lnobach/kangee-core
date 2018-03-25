package de.roo.themes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.roo.themes.ITheme.ThemeDesc;
import de.roo.themes.candy.Candy;
import de.roo.themes.kangaroo.Kangaroo;
import de.roo.themes.kangarooNoanim.KangarooNoanim;
import de.roo.themes.raw.Pure;

/**
 * 
 * @author Leo Nobach
 *
 */
public class Themes {

	static Map<String, ITheme> themes = new HashMap<String, ITheme>();
	static List<ITheme> themesAsList = new ArrayList<ITheme>();
	
	static {
		add(new Kangaroo());
		add(new KangarooNoanim());
		add(new Candy());
		add(new Pure());
	}
	
	public static void add(ITheme theme) {
		themesAsList.add(theme);
		themes.put(getDesc(theme).key(), theme);
	}
	
	public static Map<String, ITheme> getThemesAsMap() {
		return Collections.unmodifiableMap(themes);
	}
	
	public static List<ITheme> getThemesAsList() {
		return Collections.unmodifiableList(themesAsList);
	}
	
	public static ThemeDesc getDesc(ITheme theme) {
		ThemeDesc desc = theme.getClass().getAnnotation(ThemeDesc.class);
		if (desc == null) throw new IllegalStateException("Themes always need an annotation of type ITheme.ThemeDesc");
		return desc;
	}
	
}
