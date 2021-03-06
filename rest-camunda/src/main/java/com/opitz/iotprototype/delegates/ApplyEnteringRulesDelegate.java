package com.opitz.iotprototype.delegates;

import com.opitz.iotprototype.entities.DeviceGroup;
import com.opitz.iotprototype.entities.User;
import com.opitz.iotprototype.services.UserService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.ObjectFilter;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashSet;

/**
 * Load {@link User} by execution variable 'username' and apply all drools rules
 * on it. See {@link ApplyEnteringRulesDelegate#ENTERING_RULES_FILE} for rules.
 * Resulted {@link DeviceGroup}s are set in execution variable 'specials' as
 * {@link HashSet} for further processing steps.
 * 
 * @author developer
 * 
 */
public class ApplyEnteringRulesDelegate implements JavaDelegate {

	private static final String ENTERING_RULES_FILE = "enteringRules.drl";

	@Autowired
	private UserService userService;

	@Override
	public void execute(DelegateExecution execution) throws Exception {

		System.out.println("## camunda: apply entering rules by file "
		    + ENTERING_RULES_FILE + " ##");

		KnowledgeBase knowledgeBase = createKnowledgeBase();
		StatefulKnowledgeSession workingMemory = knowledgeBase.newStatefulKnowledgeSession();

		String username = (String) execution.getVariable("username");
		User user = userService.load(username);

		try {
			workingMemory.insert(user);

			workingMemory.fireAllRules();

			Collection<Object> result = this.filterFacts(workingMemory, HashSet.class);

			// drools return a single object, so only first array element is
			// processed as a Set
			if (result.isEmpty()) {
				execution.setVariable("specials", new HashSet<String>());
			} else {
				@SuppressWarnings("unchecked")
				HashSet<String> specials = (HashSet<String>) result.toArray()[0];
				execution.setVariable("specials", specials);
			}
		} finally {
			workingMemory.dispose();
		}
	}

	/**
	 * Filter facts from session.
	 * 
	 * @return {@link Collection} of all facts matched by given {@link Class}
	 */
	private Collection<Object> filterFacts(
	    final StatefulKnowledgeSession session,
	    @SuppressWarnings("rawtypes") final Class factClass) {
		ObjectFilter filter = new ObjectFilter() {

			@Override
			public boolean accept(Object object) {
				return object.getClass().equals(factClass);
			}
		};
		Collection<Object> results = session.getObjects(filter);
		return results;
	}

	/**
	 * Create new knowledge base.
	 * 
	 * @return new {@link KnowledgeBase}
	 */
	private static KnowledgeBase createKnowledgeBase() {
		KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		builder.add(ResourceFactory.newClassPathResource(ENTERING_RULES_FILE),
		    ResourceType.DRL);

		if (builder.hasErrors()) {
			throw new RuntimeException(builder.getErrors().toString());
		}
		KnowledgeBase knowledgeBase = KnowledgeBaseFactory.newKnowledgeBase();
		knowledgeBase.addKnowledgePackages(builder.getKnowledgePackages());
		return knowledgeBase;
	}

}
