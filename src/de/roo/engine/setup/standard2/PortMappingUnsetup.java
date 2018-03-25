package de.roo.engine.setup.standard2;

import de.roo.configuration.IConf;
import de.roo.engine.setup.IUnSetup;
import de.roo.logging.ILog;
import de.roo.portmapping.IPortMappingEntry;
import de.roo.portmapping.PortMappingException;

/**
 * 
 * @author Leo Nobach
 *
 */
public class PortMappingUnsetup implements IUnSetup {

	private IPortMappingEntry hdl;
	private IConf conf;

	public PortMappingUnsetup(IPortMappingEntry hdl, IConf conf) {
		this.hdl = hdl;
		this.conf = conf;
	}
	
		@Override
		public void execute(ILog log) {
			log.dbg(this, "Undoing port mapping");
			if (conf.getValueBoolean("UndoPortMapping", true)) {
				try {
					hdl.removePortMapping(log);
				} catch (PortMappingException e) {
					log.warn(this, "Could not remove portmapping. ", e);
				}
			}
		}

}
