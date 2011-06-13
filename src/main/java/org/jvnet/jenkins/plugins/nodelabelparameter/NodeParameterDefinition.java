/**
 * 
 */
package org.jvnet.jenkins.plugins.nodelabelparameter;

import hudson.Extension;
import hudson.model.ParameterValue;
import hudson.model.SimpleParameterDefinition;
import hudson.model.ComputerSet;
import hudson.model.Hudson;
import hudson.model.ParameterDefinition;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author domi
 * 
 */
public class NodeParameterDefinition extends SimpleParameterDefinition {

	private static final long serialVersionUID = 1L;

	public static final String ALL_NODES = "ALL (no restriction)";

	public final List<String> allowedSlaves;
	public final String defaultValue;

	@DataBoundConstructor
	public NodeParameterDefinition(String name, String description,
			String defaultValue, List<String> allowedSlaves) {
		super(name, description);
		this.allowedSlaves = allowedSlaves;
		this.defaultValue = defaultValue;
	}

	/**
	 * e.g. what to show if a build is triggered by hand?
	 */
	@Override
	public NodeParameterValue getDefaultParameterValue() {
		NodeParameterValue v = new NodeParameterValue(getName(), defaultValue,
				getDescription());
		return v;
	}

	@Override
	public ParameterValue createValue(String value) {
		return new NodeParameterValue(getName(), value, getDescription());
	}

	@Override
	public ParameterDefinition copyWithDefaultValue(ParameterValue defaultValue) {
		if (defaultValue instanceof NodeParameterValue) {
			NodeParameterValue value = (NodeParameterValue) defaultValue;
			return new NodeParameterDefinition(getName(), getDescription(),
					value.label, getSlaveNames());
		} else {
			return this;
		}
	}

	/**
	 * Returns a list of nodes the job could run on. If allowed nodes is empty,
	 * it falls back to all nodes
	 * 
	 * @return list of nodenames.
	 */
	public List<String> getAllowedNodesOrAll() {
		return allowedSlaves == null || allowedSlaves.isEmpty()
				|| allowedSlaves.contains(ALL_NODES) ? getSlaveNames()
				: allowedSlaves;
	}

	/**
	 * returns all available nodes plus an identifier to identify all slaves at
	 * position one.
	 * 
	 * @return list of node names
	 */
	public static List<String> getSlaveNamesForSelection() {
		List<String> slaveNames = getSlaveNames();
		slaveNames.add(0, ALL_NODES);
		return slaveNames;
	}

	/**
	 * Gets the names of all configured slaves, regardless whether they are
	 * online.
	 * 
	 * @return list with all slave names
	 */
	@SuppressWarnings("deprecation")
	public static List<String> getSlaveNames() {
		ComputerSet computers = Hudson.getInstance().getComputer();
		List<String> slaveNames = computers.get_slaveNames();

		// slaveNames is unmodifiable, therefore create a new list
		List<String> test = new ArrayList<String>();
		test.addAll(slaveNames);

		// add 'magic' name for master, so all nodes can be handled the same way
		if (!test.contains("master")) {
			test.add("master");
		}
		return test;
	}

	@Extension
	public static class DescriptorImpl extends ParameterDescriptor {
		@Override
		public String getDisplayName() {
			return "Node";
		}

		@Override
		public String getHelpFile() {
			return "/plugin/nodelabelparameter/nodeparam.html";
		}
	}

	@Override
	public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
		NodeParameterValue value = req.bindJSON(NodeParameterValue.class, jo);
		value.setDescription(getDescription());
		return value;
	}

}