package org.d9i.java.sparkweave.epsilonNetwork;

import org.coode.owlapi.turtle.TurtleOntologyFormat;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;

import org.semanticweb.HermiT.Reasoner;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/*
 * This has the goal to produce an optimised ontology for the epsilon network based on the complete schema and
 * the patterns. The schema and patterns need to be specified as OWL ontologies. Only the following constructs
 * are allowed:
 * - owl:Class (schema and patterns)
 * - owl:ObjectProperty (schema and patterns)
 * - owl:DatatypeProperty (schema and patterns)
 * - rdfs:subClassOf (schema)
 * - rdfs:subPropertyOf (schema)
 * - rdfs:domain (schema)
 * - rdfs:range (schema)
 * - owl:inverseOf (schema)
 * - owl:SymmetricProperty (schema)
 * 
 * The epsilon-schema is output as an OWL ontology. It is a subset of the complete schema, and it supports only
 * a subset of the entailments.
 */

public class EpsilonSchemaOptimiser
{
	
	public static void main(String[] args)
	{
		try
		{
			/*
			 * Initialisation phase.
			 */
			
			// Initialise ontology manager and data factory
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLDataFactory factory = manager.getOWLDataFactory();
			
			// Load schema and patterns from files
			File schemaFile = new File("ontologies/sma_epsilon.owl");
			File patternsFile = new File("ontologies/sma_patterns.owl");
			OWLOntology schema = manager.loadOntologyFromOntologyDocument(schemaFile);
			OWLOntology patterns = manager.loadOntologyFromOntologyDocument(patternsFile);
			
			// Extract set of properties from the schema
			Set<OWLDataProperty> schemaDataProperties = schema.getDataPropertiesInSignature();
			Set<OWLObjectProperty> schemaObjectProperties = schema.getObjectPropertiesInSignature();
			
			// Extract sets of classes and properties from patterns
			Set<OWLClass> patternClasses = patterns.getClassesInSignature();
			Set<OWLDataProperty> patternDataProperties = patterns.getDataPropertiesInSignature();
			Set<OWLObjectProperty> patternObjectProperties = patterns.getObjectPropertiesInSignature();
			
			// Connect reasoner to schema
			OWLReasonerFactory reasonerFactory = new Reasoner.ReasonerFactory();
			OWLReasoner schemaReasoner = reasonerFactory.createReasoner(schema);
			schemaReasoner.precomputeInferences();

			// Create empty epsilon-schema
			OWLOntology epsilonSchema = manager.createOntology();
			
			
			/*
			 * We start with adding to the epsilon-schema all the classes in the patterns and their subclasses.
			 */
			
			// For each class in the patterns
			for (OWLClass superclass : patternClasses)
			{
				// If the class is already in epsilon-schema we jump to the next class
				if (epsilonSchema.containsClassInSignature(superclass.getIRI()))
				{
					continue;
				}
				// Add declaration axiom for the class
				manager.addAxiom(epsilonSchema,factory.getOWLDeclarationAxiom(superclass));
				// Get all subclasses in the schema (direct and indirect)
				NodeSet<OWLClass> subclasses = schemaReasoner.getSubClasses(superclass, false);
				// Add subclasses to epsilon-schema: for each subclass 
				for (OWLClass subclass : subclasses.getFlattened())
				{	
					if (!subclass.isOWLNothing() && !subclass.isAnonymous())
					{
						// Add declaration axiom
						manager.addAxiom(epsilonSchema,factory.getOWLDeclarationAxiom(subclass));
						// Add subclass axiom
						manager.addAxiom(epsilonSchema,factory.getOWLSubClassOfAxiom(subclass, superclass));
					}
				}
			}
			
			
			/*
			 * Now we add relevant data properties, i.e:
			 * 1) data properties in the patterns and their subproperties;
			 * 2) data properties whose domain belongs to pattern classes.
			 */

			// We need to consider all data properties in the schema, and not just those in the patterns, because of case 2
			// For each data property in the schema
			for (OWLDataProperty property : schemaDataProperties)
			{
				// If the property is already in epsilon-schema we jump to the next
				if (epsilonSchema.containsDataPropertyInSignature(property.getIRI()))
				{
					continue;
				}
				// If the property is not in the patterns we need to check its domain (case 2 above)
				if (!patternDataProperties.contains(property))
				{
					// Get indirect domain (i.e. including superclasses)
					NodeSet<OWLClass> domain = schemaReasoner.getDataPropertyDomains(property, false);
					boolean classFound = false;
					// For each class in indirect domain
					for (OWLClass dclass : domain.getFlattened())
					{
						// If the class is in the patterns, we found something
						if (patternClasses.contains(dclass))
						{
							classFound = true;
							break; // one class is enough, no need to check the others
						}
					}
					// If nothing has been found we jump to the next property
					if (!classFound)
					{
						continue;
					}
				}
				// Add axiom for the property in the pattern
				manager.addAxiom(epsilonSchema,factory.getOWLDeclarationAxiom(property));
				// Get all subproperties in the schema (direct and indirect)
				NodeSet<OWLDataProperty> subproperties = schemaReasoner.getSubDataProperties(property, false);
				// Add subproperties to epsilon-schema: for each subproperty
				for (OWLDataProperty subproperty : subproperties.getFlattened())
				{
					if (!subproperty.isOWLBottomDataProperty() && !subproperty.isAnonymous())
					{
						// Add declaration axiom
						manager.addAxiom(epsilonSchema,factory.getOWLDeclarationAxiom(subproperty));
						// Add subproperty axiom
						manager.addAxiom(epsilonSchema,factory.getOWLSubDataPropertyOfAxiom(subproperty, property));
					}
				}
			}
			
			
			/*
			 * Now we add relevant object properties, i.e:
			 * 1) object properties in the patterns and their subproperties;
			 * 2) object properties whose domain or range belongs to pattern classes.
			 * 
			 * If an added property is symmetric in the schema, we add this axiom also to epsilon-schema.
			 */
			
			// We need to consider all object properties in the schema, and not just those in the patterns, because of case 2
			// For each object property in the schema
			for (OWLObjectProperty property : schemaObjectProperties)
			{
				// If the property is already in epsilon-schema we jump to the next
				if (epsilonSchema.containsObjectPropertyInSignature(property.getIRI()))
				{
					continue;
				}
				// If the property is not in the patterns we need to check its domain and range (case 2 above)
				if (!patternObjectProperties.contains(property))
				{
					// Get indirect domain and range (i.e. including superclasses)
					NodeSet<OWLClass> domain = schemaReasoner.getObjectPropertyDomains(property, false);
					NodeSet<OWLClass> range = schemaReasoner.getObjectPropertyRanges(property, false);
					boolean classFound = false;
					// Check domain: for each class in indirect domain
					for (OWLClass dclass : domain.getFlattened())
					{
						// If the class is in the patterns, we found something
						if (patternClasses.contains(dclass))
						{
							classFound = true;
							break; // one class is enough, no need to check the others
						}
					}
					// If nothing found, we check range
					for (OWLClass rclass : range.getFlattened())
					{
						if (patternClasses.contains(rclass))
						{
							classFound = true;
							break; // one class is enough, no need to check the others
						}
					}
					// If nothing has been found we jump to the next property
					if (!classFound)
					{
						continue;
					}
				}
				// Add axiom for the property in the pattern
				manager.addAxiom(epsilonSchema, factory.getOWLDeclarationAxiom(property));
				// If the property is symmetric, add symmetric axiom
				if (property.isSymmetric(schema))
				{
					manager.addAxiom(epsilonSchema, factory.getOWLSymmetricObjectPropertyAxiom(property));
				}
				// Get all subproperties in the schema (direct and indirect)
				NodeSet<OWLObjectPropertyExpression> subproperties = schemaReasoner.getSubObjectProperties(property, false);
				// Add subproperties to epsilon-schema: for each subproperty
				for (OWLObjectPropertyExpression subpropertyExp : subproperties.getFlattened())
				{
					if (!subpropertyExp.isAnonymous())
					{
						OWLObjectProperty subproperty = subpropertyExp.asOWLObjectProperty();
						if (!subproperty.isOWLBottomObjectProperty())
						{
							// Add declaration axiom
							manager.addAxiom(epsilonSchema,factory.getOWLDeclarationAxiom(subproperty));
							// Add subproperty axiom
							manager.addAxiom(epsilonSchema,factory.getOWLSubObjectPropertyOfAxiom(subproperty, property));
							// If symmetric, add symmetric axiom
							if (subproperty.isSymmetric(schema))
							{
								manager.addAxiom(epsilonSchema, factory.getOWLSymmetricObjectPropertyAxiom(subproperty));
							}
						}
					}
				}
			}
			
			
			/*
			 * Now we add axioms regarding inverse properties.
			 */
			
			// For each inverse property axiom in the schema
			for (OWLInverseObjectPropertiesAxiom axiom : schema.getAxioms(AxiomType.INVERSE_OBJECT_PROPERTIES))
			{
				// Extract the two properties from the axiom
				OWLObjectProperty firstProperty = axiom.getFirstProperty().asOWLObjectProperty();
				OWLObjectProperty secondProperty = axiom.getSecondProperty().asOWLObjectProperty();
				// If one of the properties is in the epsilon-schema, we add the other property and the inverse axiom
				// Check the first property
				if (epsilonSchema.containsEntityInSignature(firstProperty))
				{
					// Add inverse property axiom
					manager.addAxiom(epsilonSchema, axiom);
					// Add declaration axiom for the other property (if needed)
					if (!epsilonSchema.containsEntityInSignature(secondProperty))
					{
						manager.addAxiom(epsilonSchema,factory.getOWLDeclarationAxiom(secondProperty));
					}
					// No need to check the other property
					continue;
				}
				// Check the second property
				if (epsilonSchema.containsEntityInSignature(secondProperty))
				{
					// Add inverse property axiom
					manager.addAxiom(epsilonSchema, axiom);
					// Add declaration axiom for the other property (if needed)
					if (!epsilonSchema.containsEntityInSignature(firstProperty))
					{
						manager.addAxiom(epsilonSchema,factory.getOWLDeclarationAxiom(firstProperty));
					}
				}
			}
			
			
			/*
			 * At this point all relevant classes and properties have been added to epsilon-schema, but
			 * the two hierarchies are not yet connected through domain and range of the properties.
			 */
			
			// Extract sets of properties from epsilon-schema
			Set<OWLDataProperty> epsilonDataProperties = epsilonSchema.getDataPropertiesInSignature();
			Set<OWLObjectProperty> epsilonObjectProperties = epsilonSchema.getObjectPropertiesInSignature();

			
			/*
			 * Now we add domain axioms for data properties.
			 */
			
			// For each data property in epsilon-schema
			for (OWLDataProperty property : epsilonDataProperties)
			{
				// We get both direct and indirect domain from schema
				NodeSet<OWLClass> domain = schemaReasoner.getDataPropertyDomains(property, false);
				// For each class in the domain
				for (OWLClass dclass : domain.getFlattened())
				{
					// We add the domain axiom to epsilon-schema only if the class is in the patterns.
					// The indirect domain includes superclasses, so there is no need to add axioms for
					// classes which are in the epsilon-schema but not in the patterns, as we are not
					// interested in them, but only in their superclasses in the patterns.
					if (patternClasses.contains(dclass))
					{
						manager.addAxiom(epsilonSchema, factory.getOWLDataPropertyDomainAxiom(property, dclass));
					}
				}
			}
			
			
			/*
			 * Now we add domain and range axioms for object properties.
			 */
			
			// For each object property in epsilon-schema
			for (OWLObjectProperty property : epsilonObjectProperties)
			{
				// We get both direct and indirect domain from schema
				NodeSet<OWLClass> domain = schemaReasoner.getObjectPropertyDomains(property, false);
				for (OWLClass dclass : domain.getFlattened())
				{
					// We add the domain axiom to epsilon-schema only if the class is in the patterns.
					// The indirect domain includes superclasses, so there is no need to add axioms for
					// classes which are in the epsilon-schema but not in the patterns, as we are not
					// interested in them, but only in their superclasses in the patterns.
					if (patternClasses.contains(dclass))
					{
						manager.addAxiom(epsilonSchema, factory.getOWLObjectPropertyDomainAxiom(property, dclass));
					}
				}
				// We get both direct and indirect range from schema
				NodeSet<OWLClass> range = schemaReasoner.getObjectPropertyRanges(property, false);
				for (OWLClass rclass : range.getFlattened())
				{				
					// We add the range axiom to epsilon-schema only if the class is in the patterns.
					// The indirect range includes superclasses, so there is no need to add axioms for
					// classes which are in the epsilon-schema but not in the patterns, as we are not
					// interested in them, but only in their superclasses in the patterns.
					if (patternClasses.contains(rclass))
					{
						manager.addAxiom(epsilonSchema, factory.getOWLObjectPropertyRangeAxiom(property, rclass));
					}
				}				
			}
			
			
			/*
			 * At this point all needed axioms have been added to epsilon-schema. Now we need to delete
			 * the unneeded ones.
			 */
									
			// Connect epsilon-schema to reasoner
			OWLReasoner epsilonReasoner = reasonerFactory.createReasoner(epsilonSchema);
			epsilonReasoner.precomputeInferences();
			
			
			/*
			 * We remove all indirect subclass axioms from epsilon-schema.
			 * 
			 * If:
			 * 1) :A rdfs:subClassOf :B .
			 * 2) :A rdfs:subClassOf :C .
			 * 3) :B rdfs:subClassOf :C .
			 * We remove axiom 2, because it is not needed and it introduces a double entailment path.
			 * 
			 * Note that this can happen only when both classes :B and :C are in the patterns, due to
			 * how the class hierarchy in epsilon-schema was built (subclass axioms were added only for
			 * superclasses in the patterns). 
			 */
			
			// For each class in the pattern (other classes cannot have indirect subclasses in epsilon-schema)
			for (OWLClass superclass : patternClasses)
			{
				// Get all subclasses and direct subclasses in epsilon-schema
				NodeSet<OWLClass> subclasses = epsilonReasoner.getSubClasses(superclass, false);
				NodeSet<OWLClass> directSubclasses = epsilonReasoner.getSubClasses(superclass, true);
				// For each subclass 
				for (OWLClass subclass : subclasses.getFlattened())
				{
					// If it is not a direct subclass, remove the subclass axiom
					if (!directSubclasses.containsEntity(subclass))
					{
						OWLSubClassOfAxiom axiom = factory.getOWLSubClassOfAxiom(subclass, superclass);
						manager.removeAxiom(epsilonSchema, axiom);
					}
				}
			}
			
			
			/*
			 * We remove all indirect data subproperty axioms from epsilon-schema.
			 * 
			 * If:
			 * 1) :P rdfs:subPropertyOf :Q .
			 * 2) :P rdfs:subPropertyOf :R .
			 * 3) :Q rdfs:subPropertyOf :R .
			 * We remove axiom 2, because it is not needed and it introduces a double entailment path.
			 * 
			 * Note that this can happen only when both properties :Q and :R are in the patterns, due to
			 * how the property hierarchy in epsilon-schema was built (subproperty axioms were added only
			 * for superproperties in the patterns). 
			 */
			
			// For each data property in the pattern (other data properties cannot have indirect subproperties in epsilon-schema)
			for (OWLDataProperty superproperty : patternDataProperties)
			{
				// Get all subproperties and direct subproperties in epsilon-schema
				NodeSet<OWLDataProperty> subproperties = epsilonReasoner.getSubDataProperties(superproperty, false);
				NodeSet<OWLDataProperty> directSubproperties = epsilonReasoner.getSubDataProperties(superproperty, true);
				// For each subproperty
				for (OWLDataProperty subproperty : subproperties.getFlattened())
				{
					// If it is not a direct subproperty, remove the subproperty axiom
					if (!directSubproperties.containsEntity(subproperty))
					{
						OWLSubDataPropertyOfAxiom axiom = factory.getOWLSubDataPropertyOfAxiom(subproperty, superproperty);
						manager.removeAxiom(epsilonSchema, axiom);
					}
				}
			}
			
			
			/*
			 * In the same way we remove all indirect object subproperties axioms from epsilon-schema.
			 */
			
			// For each object property in the pattern (other object properties cannot have indirect subproperties in epsilon-schema)
			for (OWLObjectProperty superproperty : patternObjectProperties)
			{
				// Get all subproperties and direct subproperties in epsilon-schema
				NodeSet<OWLObjectPropertyExpression> subproperties = epsilonReasoner.getSubObjectProperties(superproperty, false);
				NodeSet<OWLObjectPropertyExpression> directSubproperties = epsilonReasoner.getSubObjectProperties(superproperty, true);
				// For each subproperty
				for (OWLObjectPropertyExpression subpropertyExp : subproperties.getFlattened())
				{	
					if (!subpropertyExp.isAnonymous())
					{
						OWLObjectProperty subproperty = subpropertyExp.asOWLObjectProperty();
						// If it is not a direct subproperty, remove the subproperty axiom
						if (!directSubproperties.containsEntity(subproperty))
						{
							OWLSubObjectPropertyOfAxiom axiom = factory.getOWLSubObjectPropertyOfAxiom(subproperty, superproperty);
							manager.removeAxiom(epsilonSchema, axiom);
						}
					}
				}
			}

			
			/*
			 * Now we remove redundant domain and range axioms regarding entailments that can be got
			 * thorugh the subproperty hierarchy.
			 * 
			 * If:
			 * 1) :P rdfs:subPropertyOf :Q .
			 * 2) :P rdfs:domain :A .
			 * 3) :Q rdfs:domain :A. 
			 * We remove axiom 2, because it is not needed and it introduces a double entailment path.
			 * The same applies to property range.
			 */
			
			// For each object property in epsilon-schema
			for (OWLObjectProperty property : epsilonObjectProperties)
			{
				// Get superproperties in epsilon-schema
				NodeSet<OWLObjectPropertyExpression> superproperties = epsilonReasoner.getSuperObjectProperties(property, false);
				// Get superproperties domains and ranges
				Set<OWLClass> superDomain = new HashSet<OWLClass>();
				Set<OWLClass> superRange = new HashSet<OWLClass>();
				for (OWLObjectPropertyExpression superpropertyExp : superproperties.getFlattened())
				{
					superDomain.addAll(epsilonReasoner.getObjectPropertyDomains(superpropertyExp, true).getFlattened());
					superRange.addAll(epsilonReasoner.getObjectPropertyRanges(superpropertyExp, true).getFlattened());
				}
				// Get domain and direct domain (i.e. domain without considering superclasses) 
				NodeSet<OWLClass> domain = epsilonReasoner.getObjectPropertyDomains(property, false);
				NodeSet<OWLClass> directDomain = epsilonReasoner.getObjectPropertyDomains(property, true);
				// Get range and direct range (i.e. range without considering superclasses)
				NodeSet<OWLClass> range = epsilonReasoner.getObjectPropertyRanges(property, false);
				NodeSet<OWLClass> directRange = epsilonReasoner.getObjectPropertyRanges(property, true);
				// For each class in domain
				for (OWLClass dclass : domain.getFlattened())
				{
					// If the class is not in direct domain, or it is in superproperties domain, remove domain axiom
					if (!directDomain.containsEntity(dclass) || superDomain.contains(dclass))
					{
						OWLObjectPropertyDomainAxiom axiom = factory.getOWLObjectPropertyDomainAxiom(property, dclass);
						manager.removeAxiom(epsilonSchema, axiom);
					}					
				}
				// For each class in range
				for (OWLClass rclass : range.getFlattened())
				{
					// If the class is not in direct range, remove range axiom
					if (!directRange.containsEntity(rclass) || superRange.contains(rclass))
					{
						OWLObjectPropertyRangeAxiom axiom = factory.getOWLObjectPropertyRangeAxiom(property, rclass);							
						manager.removeAxiom(epsilonSchema, axiom);
					}
				}
			}
			// Now we do the same for data properties
			// For each data property in epsilon-schema
			for (OWLDataProperty property : epsilonDataProperties)
			{
				// Get superproperties in epsilon-schema
				NodeSet<OWLDataProperty> superproperties = epsilonReasoner.getSuperDataProperties(property, false);
				// Get superproperties domains
				Set<OWLClass> superDomain = new HashSet<OWLClass>();
				for (OWLDataProperty superproperty : superproperties.getFlattened())
				{
					superDomain.addAll(epsilonReasoner.getDataPropertyDomains(superproperty, true).getFlattened());
				}
				// Get domain and direct domain (i.e. domain without considering superclasses)
				NodeSet<OWLClass> domain = epsilonReasoner.getDataPropertyDomains(property, false);
				NodeSet<OWLClass> directDomain = epsilonReasoner.getDataPropertyDomains(property, true);
				// For each class in domain
				for (OWLClass dclass : domain.getFlattened())
				{
					// If the class is not in direct domain, or it is in superproperties domain, remove domain axiom
					if (!directDomain.containsEntity(dclass) || superDomain.contains(dclass))
					{
						OWLDataPropertyDomainAxiom axiom = factory.getOWLDataPropertyDomainAxiom(property, dclass);
						manager.removeAxiom(epsilonSchema, axiom);
					}					
				}
			}
			
			
			/*
			 * For each pair of inverse properties in epsilon-schema, we delete domain/range axioms for
			 * one of the properties, as they are redundant.
			 */
			
			Set<OWLObjectProperty> inversePropertiesDRRemoved = new HashSet<OWLObjectProperty>();
			// For each inverse property axiom in epsilon-schema
			for (OWLInverseObjectPropertiesAxiom axiom : epsilonSchema.getAxioms(AxiomType.INVERSE_OBJECT_PROPERTIES))
			{
				OWLObjectProperty firstProperty = axiom.getFirstProperty().asOWLObjectProperty();
				OWLObjectProperty secondProperty = axiom.getSecondProperty().asOWLObjectProperty();
				// We need to be sure that we do not get the same pair twice
				if (!inversePropertiesDRRemoved.contains(firstProperty) && !inversePropertiesDRRemoved.contains(secondProperty))
				{
					// Add the first property to the set of already processed ones
					inversePropertiesDRRemoved.add(firstProperty);
					// Remove domain axioms
					for (OWLObjectPropertyDomainAxiom domainAxiom : epsilonSchema.getObjectPropertyDomainAxioms(firstProperty))
					{
						manager.removeAxiom(epsilonSchema, domainAxiom);
					}
					// Remove range axioms
					for (OWLObjectPropertyRangeAxiom rangeAxiom : epsilonSchema.getObjectPropertyRangeAxioms(firstProperty))
					{
						manager.removeAxiom(epsilonSchema, rangeAxiom);
					}
				}
			}
			
			
			/*
			 * Finished. We now output epsilon-schema.
			 */
			
			// Select Turtle format
			TurtleOntologyFormat epsilonFormat = new TurtleOntologyFormat();
			// Copy prefixes and from schema
			epsilonFormat.copyPrefixesFrom(manager.getOntologyFormat(schema).asPrefixOWLOntologyFormat());
			// Save epsilon-schema as file
			File epsilonFile = new File("ontologies/sma_epsilon_optimized.owl");
			manager.saveOntology(epsilonSchema, epsilonFormat, IRI.create(epsilonFile.toURI()));
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
}
