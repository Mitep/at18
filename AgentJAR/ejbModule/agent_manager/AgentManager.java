package agent_manager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import model.acl.ACLMessage;
import model.agent.AID;
import model.agent.AgentClass;
import model.agent.AgentType;

@Singleton
@Startup
public class AgentManager implements AgentManagerLocal {

	private HashMap<AID, AgentClass> runningAgents;
	private List<AgentType> agentTypes;

	public AgentManager() {
	}

	@PostConstruct
	private void initAgentManager() {
		runningAgents = new HashMap<AID, AgentClass>();
		initAgentTypes();
	}

	private void initAgentTypes() {
		agentTypes = new ArrayList<AgentType>();

		final File basePackage = new File(
				AgentManagerLocal.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "\\agents");

		agentTypes = processFile(basePackage);

	}

	private ArrayList<AgentType> processFile(File f) {
		ArrayList<AgentType> types = new ArrayList<>();
		if (f.isDirectory()) {
			for (File file : f.listFiles()) {
				ArrayList<AgentType> tmp = processFile(file);
				types.addAll(tmp);
			}
		}

		if (f.isFile()) {
			File parent = f.getParentFile();
			String module = parent.getPath().substring(parent.getPath().indexOf("agents"));
			module = module.replace(File.separatorChar, '.');
			String name = f.getName();
			name = name.substring(0, name.indexOf("."));
			AgentType at = new AgentType(name, module);
			types.add(at);
		}

		return types;
	}

	@Override
	public List<AID> getRunningAgents() {
		return new ArrayList<>(runningAgents.keySet());
	}

	@Override
	public List<AgentType> getAgentTypes() {
		return agentTypes;
	}

	@Override
	public boolean msgToAgent(AID agent, ACLMessage msg) {
		AgentClass receiver = runningAgents.get(agent);
		if (receiver != null) {
			receiver.handleMessage(msg);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void startAgent(AID agent) {
		if (runningAgents.containsKey(agent)) {
			System.out.println("Vec postoji agent s tim identifikatorom!");
		}

		try {
			Object obj = Class.forName(agent.getType().toString()).newInstance();
			if (obj instanceof AgentClass) {
				runningAgents.put(agent, (AgentClass) obj);
			} else {
				System.out.println("Agent tipa " + agent.getType() + " se dodati u mapu!");
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void stopAgent(AID agent) {
		if (runningAgents.containsKey(agent)) {
			runningAgents.remove(agent);
		} else {
			System.out.println("Ne postoji agent s tim identifikatorom!");
		}
	}

}