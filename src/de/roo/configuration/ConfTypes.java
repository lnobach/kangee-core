package de.roo.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Leo Nobach
 *
 */
public class ConfTypes {

	static final Map<String, Class<?>> types = new HashMap<String, Class<?>>();
	
	static {
		add("Use_Hostnames", Boolean.class);//false
		add("configured", Boolean.class);//true
		add("Disable_Tray", Boolean.class);//false
		add("DisableLogAutoShow", Boolean.class);//true
		add("LoadPrefs", String.class);//1354,320,547,497
		add("ConnPMapForwardingIncrementLimit", Integer.class);//20
		add("ConnRemoteTestEnabled", Boolean.class);//true
		add("copyHashToo", Boolean.class);//false
		add("ConfigurationWindow", String.class);//1011,369,647,602
		add("ConnPMapAutoIncrement", Boolean.class);//true
		add("Use_Direct_Downloads", Boolean.class);//false
		add("POST_Files_Basedir", String.class);//auto
		add("ConnFixedAddress", String.class);//
		add("ResourcePrefs", String.class);//1145,351,500,500
		add("UndoPortMapping", Boolean.class);//true
		add("SingletonMode", Boolean.class);//true
		add("Sec_MaxFailedAttempts", Integer.class);//20
		add("BarcodeSizeXDetail", Integer.class);//200
		add("BarcodeSizeYDetail", Integer.class);//200
		add("BarcodeSizeXPopup", Integer.class);//200
		add("BarcodeSizeYPopup", Integer.class);//200
		add("SwingLookAndFeel", String.class);//auto
		add("Presentation_Template", String.class);//pure
		add("ConnPMapQueryEnabled", Boolean.class);//true
		add("IPv6Mode", Boolean.class);//false
		add("MainWindow", String.class);//1277,120,511,768
		add("DBG_HTTP_Protocol", Boolean.class);//false
		add("Port", Integer.class);//8080
		add("ConnForceSTUNOverAdapters", Boolean.class);//false
		add("Save_State", Boolean.class);//true
		add("ConnSTUNEnabled", Boolean.class);//true
		add("CreditsWindow", String.class);//910,329,877,646
		add("ConnPMappingEnabled", Boolean.class);//true
		add("AddUploadCurrentDir", String.class);///home/leo
		add("LogViewerWindow", String.class);//786,114,800,600
		add("Lan_Address", String.class);//auto
		add("Discovery_URL", String.class);//http://kangee.relevantmusic.de/api/conntest.php
		add("Lan_Address_Seq_Pref", String.class);//auto
		add("CheckForUpdatesStrict", Boolean.class);//false
		add("CheckForUpdates", Boolean.class);//true
		add("Nickname", String.class);//true
		add("DisableNotifications", Boolean.class);//false
		add("IfUpRecheckDelay", Integer.class); //3000
		
	}

	private static void add(String key, Class<?> value) {
		types.put(key, value);
	}
	
	public static Map<String, Class<?>> getTypesMap() {
		return Collections.unmodifiableMap(types);
	}
	
}
