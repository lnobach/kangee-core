package de.roo.engine;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

import de.roo.BuildConstants;
import de.roo.configuration.Configuration;
import de.roo.configuration.IWritableConf;
import de.roo.connectivity.ConnStateManager;
import de.roo.connectivity.ConnectivityInfo;
import de.roo.connectivity.IConnStateListener;
import de.roo.connectivity.IConnStateProvider;
import de.roo.connectivity.IConnStateProvider.ConnectivityState;
import de.roo.engine.RooEngine.SetupContextImpl;
import de.roo.engine.setup.ISetupContext;
import de.roo.engine.setup.ISetupFollower;
import de.roo.engine.setup.ISetupMethod;
import de.roo.engine.setup.IUnSetup;
import de.roo.engine.setup.SetupException;
import de.roo.engine.setup.standard2.Default2SetupMethod;
import de.roo.httpsrv.Server;
import de.roo.icons.Icons2;
import de.roo.logging.ILog;
import de.roo.model.ModelIO;
import de.roo.model.RooModel;
import de.roo.srv.RooRequestHandler;
import de.roo.srvApi.ServerException;
import de.roo.util.StringHasher;

/**
 * 
 * @author Leo Nobach
 *
 */
public abstract class RooEngine {
	
	static final String DEFAULT_ICONS_DIR = "gfx/icons";
	
	String rooID = StringHasher.hash(10);
	
	RooModel mdl;
	private ILog log;
	FileHandler fHndlr;
	//private ConnectivityInfo info = null;
	Server srv = null;
	
	IWritableConf conf;
	Stack<IUnSetup> unsetups = new Stack<IUnSetup>();
	private File appDir = null;
	Icons2 icons;
	Locker lock = null;
	
	ConnStateManager csmgr;
	
	public RooEngine(ILog log) {
		this.log = log;
	}
	
	protected void addUnsetup(IUnSetup unsetup) {
		unsetups.push(unsetup);
	}
	
	public Icons2 getIcons() {
		return icons;
	}
	
	public String getRooID() {
		return rooID;
	}
	
	public boolean init() {
		
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run() {
				close();
			}
		});
		
		//Prepare configuration
		conf = loadConfiguration();
		this.onBeforeSetup();
		
		if (conf.getValueBoolean("SingletonMode", true)) {
			lock = new Locker(getApplicationDir());
			if (!lock.tryLock(log)) {
				this.onIllegalSecondInstance();
				return false;
			}
		}
		
		icons = initIcons();
		csmgr = new ConnStateManager(this, log);
		SetupContextImpl setupctx = this.new SetupContextImpl();
		
		if (setup(setupctx, getSetupFollower())) {
		
			csmgr.activate(!setupctx.wasOverridden(), setupctx.getConnectivityInfo());
			csmgr.addListener(new ConnStateListenerImpl());
			
			mdl = ModelIO.loadModel(getStateFile(), this.getLog(), conf);
			this.fHndlr = new FileHandler(this, mdl);
		
			restartServer();
			return true;
		} 
		log.dbg(this, "The configuration did not finish.");
		return false;
	}
	
	/**
	 * Called on every attempt to launch a second instance of Kangee, although
	 * SingletonMode is set to true.
	 */
	protected abstract void onIllegalSecondInstance();

	protected Icons2 initIcons() {
		File iconsDirF;
		String iconsDirStr = System.getenv("ROO_ICONS_DIR");
		if (iconsDirStr == null) {
			iconsDirF = new File(DEFAULT_ICONS_DIR);
			log.dbg(this, "Environment variable ROO_ICONS_DIR not set, so will substitute default: " + iconsDirF.getAbsolutePath());
		} else {
			iconsDirF = new File(iconsDirStr);
			log.dbg(this, "icons dir overridden by env variable. Will use " + iconsDirF.getAbsolutePath() + " as icon directory");
		}
		return new Icons2(new FilesystemResourceManager(iconsDirF), log);
		
	}
	
	protected void onBeforeSetup() {
		//Nothing to do here
	}

	public abstract ISetupFollower getSetupFollower();
	
	protected IWritableConf loadConfiguration() {
		return Configuration.loadFromFileIfThere(getConfigurationFile());
	}

	protected File getConfigurationFile() {
		return new File(getApplicationDir(), "roo.ini");
	}

	public boolean setup(ISetupContext ctx, ISetupFollower follower) {
		ISetupMethod method = new Default2SetupMethod();
		
		try {
			method.setup(ctx, follower);
		} catch (SetupException ex) {
			log.error(this, "Setup failed.", ex);
			follower.setupFailed(ex);
			return false;
		}
		follower.setupFinished();
		return true;
	}

	protected void startRestartServer() throws ServerException {
		if (srv != null) {
			srv.stopServer();
			try {
				Thread.sleep(Server.RESTART_PAUSE);
			} catch (InterruptedException e) {
				//Nothing to do
			}
		}
		RooRequestHandler hdlr = new RooRequestHandler(this.getModel(), conf, this.getLog(), getApplicationDir(), icons, getRooID());
		srv = new Server(csmgr.getCurrentConnectivityInfo().getPort(), this.getLog(), hdlr, conf);
	}

	public RooModel getModel() {
		return mdl;
	}
	
	public ILog getLog() {
		return log;
	}
	
	public FileHandler getFileHandler() {
		return fHndlr;
	}
	
	public IWritableConf getConfiguration() {
		return conf;
	}
	
	boolean shallSave() {
		return conf.getValueBoolean("Save_State", true);
	}

	public void close() {
		unsetup();
		saveState();
		if (srv != null) srv.stopServer();
		saveConf();
		if (lock != null) lock.unlock(log);
		log.dbg(this, "Closing " + BuildConstants.PROD_TINY_NAME);
		//No System.exit, since it is called as a shutdown hook
	}

	public void unsetup() {
		while(!unsetups.isEmpty()) {
			IUnSetup unsetup = unsetups.pop();
			unsetup.execute(log);
		}
	}

	protected void saveConf() {
		try {
			Configuration.makePathAndSave(getConfigurationFile(), conf);
		} catch (IOException e) {
			log.error(this, "Error while saving configuration.", e);
		}
	}

	private void saveState() {
		if (!shallSave()) return;
		try {
			if (mdl != null) ModelIO.saveModel(getStateFile(), getModel(), getLog(), getConfiguration());
		} catch (Throwable e) {
			log.error(this, "Can not save model because of error.", e);
		}
	}
	
	protected File getStateFile() {
		return new File(getApplicationDir(), "state.xml");
	}
	
	protected abstract File determineApplicationDir();
	
	protected File getApplicationDir() {
		if (appDir == null) {
			appDir  = determineApplicationDir();
			getLog().dbg(this, "Our application directory is " + appDir.getAbsolutePath());
		}
		return appDir;
	}
	
	public class SetupContextImpl implements ISetupContext {
		
		private ConnectivityInfo info = null;

		boolean overridden = false;
		
		@Override
		public IWritableConf getConf() {
			return conf;
		}

		public boolean wasOverridden() {
			return overridden;
		}

		@Override
		public ILog getLog() {
			return RooEngine.this.getLog();
		}

		@Override
		public ConnectivityInfo getConnectivityInfo() {
			return info;
		}

		@Override
		public void setConnectivityInfo(ConnectivityInfo info) {
			this.info = info;
		}

		@Override
		public String getRooID() {
			return RooEngine.this.getRooID();
		}

		@Override
		public void addUnsetup(IUnSetup unsetup) {
			RooEngine.this.addUnsetup(unsetup);
		}
		
		@Override
		public int isServerRunning() {
			return RooEngine.this.isServerRunning();
		}

		@Override
		public void setUnsuccessfulAndOverridden() {
			overridden = true;
		}

	}
	
	public void restartServer() {
		try {
			startRestartServer();
		} catch (ServerException e) {
			this.getLog().error(this, "Could not initialize server.", e);
		}
	}
	
	public void reconfigure() {

		if (csmgr.getCurrentConnectivityState() != ConnectivityState.SettingUp) {
			new Thread("ConnResetOnReconfig"){
				
				public void run() {
					csmgr.forceReset();
				}
				
			}.start();
		}
	}
	
	public IConnStateProvider getConnStateProvider() {
		return csmgr;
	}
	
	class ConnStateListenerImpl implements IConnStateListener {

		@Override
		public void connectivityStateChanged(IConnStateProvider src,
				ConnectivityState state) {
			if (state == ConnectivityState.Available) {
				try {
					startRestartServer();
				} catch (ServerException e) {
					getLog().error(this, "Connectivity state changed, but could not restart server.", e);
				}
			}
		}

		@Override
		public void connectivityJobChanged(IConnStateProvider prov,
				String connectivityJobDesc) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	/**
	 * Returns the server port, otherwise a value smaller than 0.
	 * @return
	 */
	public int isServerRunning() {
		if (srv == null || !srv.isUp()) return -1;
		return srv.getPort();
	}
	
}
