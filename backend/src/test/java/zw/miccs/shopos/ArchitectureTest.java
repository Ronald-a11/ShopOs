package zw.miccs.shopos;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/**
 * Module dependency rules from section 3 of the technical documentation.
 * Rules allow empty packages so they hold from day one and bite as code lands.
 */
@AnalyzeClasses(packages = "zw.miccs.shopos", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

	@ArchTest
	static final ArchRule domainHasNoSpringOrJpa = noClasses()
		.that().resideInAPackage("..domain..")
		.should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta.persistence..")
		.allowEmptyShould(true);

	@ArchTest
	static final ArchRule salesDoesNotReachFiscalAdapterInternals = noClasses()
		.that().resideInAPackage("zw.miccs.shopos.sales..")
		.should().dependOnClassesThat().resideInAPackage("zw.miccs.shopos.fiscalisation.infrastructure..")
		.allowEmptyShould(true);

	@ArchTest
	static final ArchRule fiscalisationDoesNotWriteInventoryOrPayments = noClasses()
		.that().resideInAPackage("zw.miccs.shopos.fiscalisation..")
		.should().dependOnClassesThat().resideInAnyPackage("zw.miccs.shopos.inventory..", "zw.miccs.shopos.payments..")
		.allowEmptyShould(true);

	@ArchTest
	static final ArchRule noModuleUsesAnotherModulesInfrastructure = classes()
		.that().resideInAPackage("zw.miccs.shopos..")
		.should(new ArchCondition<JavaClass>("not use another module's infrastructure") {
			@Override
			public void check(JavaClass origin, ConditionEvents events) {
				String own = module(origin.getPackageName());
				for (Dependency d : origin.getDirectDependenciesFromSelf()) {
					String pkg = d.getTargetClass().getPackageName();
					String target = module(pkg);
					if (target != null && !target.equals(own) && pkg.startsWith(BASE + target + ".infrastructure")) {
						events.add(SimpleConditionEvent.violated(d, d.getDescription()));
					}
				}
			}
		})
		.allowEmptyShould(true);

	private static final String BASE = "zw.miccs.shopos.";

	private static String module(String pkg) {
		if (!pkg.startsWith(BASE)) {
			return null;
		}
		String rest = pkg.substring(BASE.length());
		int dot = rest.indexOf('.');
		return dot < 0 ? rest : rest.substring(0, dot);
	}

	@ArchTest
	static final ArchRule apiLayerDoesNotTouchInfrastructure = noClasses()
		.that().resideInAPackage("..api..")
		.should().dependOnClassesThat().resideInAPackage("..infrastructure..")
		.allowEmptyShould(true);

	@ArchTest
	static final ArchRule auditDependsOnNoOtherModule = noClasses()
		.that().resideInAPackage("zw.miccs.shopos.audit..")
		.should().dependOnClassesThat().resideInAnyPackage(
			"zw.miccs.shopos.identity..", "zw.miccs.shopos.tenancy..", "zw.miccs.shopos.branches..",
			"zw.miccs.shopos.catalogue..", "zw.miccs.shopos.pricing..", "zw.miccs.shopos.tax..",
			"zw.miccs.shopos.inventory..", "zw.miccs.shopos.sales..", "zw.miccs.shopos.payments..",
			"zw.miccs.shopos.shifts..", "zw.miccs.shopos.fiscalisation..", "zw.miccs.shopos.synchronisation..",
			"zw.miccs.shopos.reporting..", "zw.miccs.shopos.subscriptions..")
		.allowEmptyShould(true);

}
