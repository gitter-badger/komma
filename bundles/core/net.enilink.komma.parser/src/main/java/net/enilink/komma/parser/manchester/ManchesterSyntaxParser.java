package net.enilink.komma.parser.manchester;

import java.util.Collection;
import java.util.List;

import net.enilink.komma.core.URI;
import net.enilink.komma.core.URIs;
import net.enilink.komma.parser.BaseRdfParser;
import net.enilink.komma.parser.manchester.tree.Annotation;
import net.enilink.komma.parser.sparql.tree.BNode;
import net.enilink.komma.parser.sparql.tree.BooleanLiteral;
import net.enilink.komma.parser.sparql.tree.GraphNode;
import net.enilink.komma.parser.sparql.tree.IriRef;
import net.enilink.komma.parser.sparql.tree.Literal;
import net.enilink.komma.parser.sparql.tree.QName;
import net.enilink.vocab.owl.OWL;
import net.enilink.vocab.rdf.RDF;
import net.enilink.vocab.rdfs.RDFS;
import net.enilink.vocab.xmlschema.XMLSCHEMA;

import org.parboiled.Rule;
import org.parboiled.support.Var;

/**
 * Parser for Manchester OWL Syntax
 * 
 * @see <a href="http://www.w3.org/TR/owl2-manchester-syntax/">Manchester OWL
 *      Syntax</a>
 * 
 * @author Ken Wenzel
 */
public class ManchesterSyntaxParser extends BaseRdfParser {
	public IManchesterActions actions;

	public ManchesterSyntaxParser() {
		this(new IManchesterActions() {
			@Override
			public boolean createStmt(Object subject, Object predicate,
					Object object) {
				return true;
			}
		});
	}

	public ManchesterSyntaxParser(IManchesterActions actions) {
		this.actions = actions;
	}

	// 2.1 IRIs, Integers, Literals, and Entities

	public Rule AnnotatedList(Var<? extends GraphNode> source, URI property,
			Rule target) {
		return Sequence(WithAnnotations(source, property, target),
				ZeroOrMore(',', WithAnnotations(source, property, target)));
	}

	public Rule Annotation() {
		return Sequence(IriRef(), AnnotationTarget(), //
				push(new Annotation((GraphNode) pop(1), (GraphNode) pop())));
	}

	public Rule AnnotationPropertyFrame() {
		Var<GraphNode> annotationPropertyIri = new Var<>();
		return Sequence(
				"AnnotationProperty:",
				IriRef(),
				annotationPropertyIri.set((GraphNode) pop()),
				actions.createStmt(annotationPropertyIri, RDF.PROPERTY_TYPE,
						OWL.TYPE_ANNOTATIONPROPERTY),
				ZeroOrMore(FirstOf(
						Sequence(
								"Annotations:",
								AnnotatedList(annotationPropertyIri, null,
										Annotation())),
						Sequence(
								"Domain:",
								AnnotatedList(annotationPropertyIri,
										RDFS.PROPERTY_DOMAIN, IriRef())),
						Sequence(
								"Range:",
								AnnotatedList(annotationPropertyIri,
										RDFS.PROPERTY_RANGE, IriRef())),
						Sequence(
								"SubPropertyOf:",
								AnnotatedList(annotationPropertyIri,
										RDFS.PROPERTY_SUBPROPERTYOF, IriRef())))));
	}

	@SuppressWarnings("unchecked")
	public Rule WithAnnotations(Var<? extends GraphNode> source, URI property,
			Rule target) {
		return Sequence(
				Annotations(),
				target,
				createAnnotatedStmt((List<Annotation>) pop(1), source.get(),
						property, pop()));
	}

	public boolean createAnnotatedStmt(List<Annotation> annotations,
			Object source, URI property, Object target) {
		if (source != null && property != null) {
			// this is the annotated statement
			actions.createStmt(source, property, target);
		} else if (target instanceof Annotation) {
			// this is an annotated annotation
			push(target);

			source = target;
			property = URIs.createURI(((Annotation) target).getPredicate()
					.toString());
			target = ((Annotation) target).getObject();
		}

		for (Annotation annotation : annotations) {
			if (property == null) {
				actions.createStmt(source, annotation.getPredicate(),
						annotation.getObject());
			} else {
				BNode annotationNode = new BNode();
				actions.createStmt(annotationNode, RDF.PROPERTY_TYPE,
						OWL.TYPE_ANNOTATIONPROPERTY);
				actions.createStmt(annotationNode,
						OWL.PROPERTY_ANNOTATEDSOURCE, source);
				actions.createStmt(annotationNode,
						OWL.PROPERTY_ANNOTATEDPROPERTY, property);
				actions.createStmt(annotationNode,
						OWL.PROPERTY_ANNOTATEDTARGET, target);

				actions.createStmt(annotationNode, annotation.getPredicate(),
						annotation.getObject());
			}
		}

		return true;
	}

	public Rule Annotations() {
		return Sequence(
				push(LIST_BEGIN),
				ZeroOrMore(
						"Annotations:",
						AnnotatedList(new Var<>((GraphNode) null), null,
								Annotation())), push(popList(Annotation.class)));
	}

	public Rule AnnotationTarget() {
		return FirstOf(BlankNode(), IriRef(), Literal());
	}

	// 2.2 Ontologies and Annotations

	public Rule Atomic() {
		return FirstOf(IriRef(), Sequence('{', List(Individual()), '}'),
				Sequence('(', Description(), ')'));
	}

	public Rule ClassFrame() {
		Var<GraphNode> classIri = new Var<>();
		return Sequence(
				"Class:",
				IriRef(),
				classIri.set((GraphNode) pop()),
				actions.createStmt(classIri, RDF.PROPERTY_TYPE, OWL.TYPE_CLASS),
				ZeroOrMore(FirstOf(
						Sequence("Annotations:",
								AnnotatedList(classIri, null, Annotation())),
						Sequence(
								"SubClassOf:",
								AnnotatedList(classIri,
										RDFS.PROPERTY_SUBCLASSOF, Description())),
						Sequence(
								"EquivalentTo:",
								AnnotatedList(classIri,
										OWL.PROPERTY_EQUIVALENTCLASS,
										Description())),
						Sequence(
								"DisjointWith:",
								AnnotatedList(classIri,
										OWL.PROPERTY_DISJOINTWITH,
										Description())),
						Sequence(
								"DisjointUnionOf:",
								WithAnnotations(
										classIri,
										OWL.PROPERTY_DISJOINTUNIONOF,
										Sequence(
												List2(Description()),
												push(createRdfList((List<?>) pop()))))),
						Sequence(
								"HasKey:",
								WithAnnotations(classIri, OWL.PROPERTY_HASKEY,
										OneOrMore(PropertyExpression()))))));
	}

	public Rule PropertyExpression() {
		return FirstOf(ObjectPropertyExpression(), DataPropertyExpression());
	}

	public Rule Conjunction() {
		return FirstOf(
				Sequence(IriRef(), "that", Optional("not"), Restriction(),
						ZeroOrMore("and", Optional("not"), Restriction())),

				Sequence(
						Primary(),
						Optional(push(LIST_BEGIN), OneOrMore("and", Primary()),
								push(new BNode()), //
								actions.createStmt(peek(), RDF.PROPERTY_TYPE,
										OWL.TYPE_CLASS), //
								actions.createStmt(
										peek(),
										OWL.PROPERTY_INTERSECTIONOF,
										createRdfList(popList(1,
												GraphNode.class, 1))))));
	}

	public GraphNode createRdfList(Collection<? extends Object> elements) {
		GraphNode head = null, last = null;

		for (Object element : elements) {
			GraphNode current = new BNode();
			actions.createStmt(current, RDF.PROPERTY_TYPE, RDF.TYPE_LIST);
			actions.createStmt(current, RDF.PROPERTY_FIRST, element);
			if (last != null) {
				actions.createStmt(last, RDF.PROPERTY_REST, current);
			}
			if (head == null) {
				head = current;
			}
			last = current;
		}
		actions.createStmt(last, RDF.PROPERTY_REST, RDF.NIL);

		return head;
	}

	public Rule DataAtomic() {
		return FirstOf(DatatypeRestriction(),
				Sequence('{', List(Literal()), '}'),
				Sequence('(', DataRange(), ')'));
	}

	public Rule DataConjunction() {
		return Sequence(
				DataPrimary(),
				Optional(push(LIST_BEGIN), OneOrMore("and", DataPrimary()),
						push(new BNode()), actions.createStmt(peek(),
								OWL.PROPERTY_INTERSECTIONOF,
								createRdfList(popList(1, GraphNode.class, 1)))));
	}

	public Rule DataPrimary() {
		return Sequence(Optional("not"), DataAtomic());
	}

	public Rule DataPropertyExpression() {
		return IriRef();
	}

	public Rule DataPropertyFact() {
		return Sequence(IriRef(), Literal());
	}

	public Rule DataPropertyFrame() {
		Var<GraphNode> datapropertyIri = new Var<>();
		return Sequence(
				"DataProperty:",
				IriRef(),
				datapropertyIri.set((GraphNode) pop()),
				actions.createStmt(datapropertyIri, RDF.PROPERTY_TYPE,
						OWL.TYPE_DATATYPEPROPERTY),
				ZeroOrMore(FirstOf(
						Sequence(
								"Annotations:",
								AnnotatedList(datapropertyIri, null,
										Annotation())),
						Sequence(
								"Domain:",
								AnnotatedList(datapropertyIri,
										RDFS.PROPERTY_DOMAIN, Description())),
						Sequence(
								"Range:",
								AnnotatedList(datapropertyIri,
										RDFS.PROPERTY_RANGE, DataRange())),
						// TODO Annotations
						Sequence(
								"Characteristics:",
								WithAnnotations(
										datapropertyIri,
										RDF.PROPERTY_TYPE,
										Sequence(
												"Functional",
												push(new IriRef(
														OWL.TYPE_FUNCTIONALPROPERTY
																.toString()))))),
						Sequence(
								"SubPropertyOf:",
								AnnotatedList(datapropertyIri,
										RDFS.PROPERTY_SUBPROPERTYOF,
										DataPropertyExpression())),
						Sequence(
								"EquivalentTo:",
								AnnotatedList(datapropertyIri,
										OWL.PROPERTY_EQUIVALENTPROPERTY,
										DataPropertyExpression())),
						Sequence(
								"DisjointWith:",
								AnnotatedList(datapropertyIri,
										OWL.PROPERTY_DISJOINTWITH,
										DataPropertyExpression())))));
	}

	public Rule DataRange() {
		return Sequence(
				DataConjunction(),
				Optional(push(LIST_BEGIN), OneOrMore("or", DataConjunction()),
						push(new BNode()), actions.createStmt(peek(),
								OWL.PROPERTY_UNIONOF,
								createRdfList(popList(1, GraphNode.class, 1)))));
	}

	// 2.3 Property and Datatype Expressions

	public Rule Datatype() {
		return FirstOf(
				IriRef(),
				Sequence(FirstOf("integer", "decimal", "float", "string"),
						push(new IriRef(XMLSCHEMA.NAMESPACE + match()))));
	}

	public Rule DatatypeFrame() {
		Var<GraphNode> datatypeIri = new Var<>();
		return Sequence(
				"Datatype:",
				Datatype(),
				datatypeIri.set((GraphNode) pop()),
				actions.createStmt(datatypeIri, RDF.PROPERTY_TYPE,
						RDFS.TYPE_DATATYPE),
				ZeroOrMore("Annotations:",
						AnnotatedList(datatypeIri, null, Annotation())),
				// TODO Annotations, dataRange
				Optional(
						"EquivalentTo:",
						WithAnnotations(datatypeIri,
								OWL.PROPERTY_EQUIVALENTCLASS, DataRange())),
				ZeroOrMore("Annotations:",
						AnnotatedList(datatypeIri, null, Annotation())));
	}

	public Rule DatatypeRestriction() {
		Var<BNode> restriction = new Var<>();
		return Sequence(
				Datatype(),
				Optional(
						'[',
						restriction.set(new BNode()),
						actions.createStmt(restriction.get(),
								RDF.PROPERTY_TYPE, RDFS.TYPE_DATATYPE),
						actions.createStmt(restriction.get(),
								OWL.PROPERTY_ONDATATYPE, pop()),

						push(LIST_BEGIN), //
						push(new BNode()), //
						Facet(), RestrictionValue(), actions.createStmt(
								peek(2), pop(1), pop()),
						ZeroOrMore(',', push(new BNode()), //
								Facet(), RestrictionValue(), //
								actions.createStmt(peek(2), pop(1), pop())),
						']', actions.createStmt(restriction.get(),
								OWL.PROPERTY_WITHRESTRICTIONS,
								createRdfList(popList(GraphNode.class))),
						push(restriction.get())));
	};

	public Rule Description() {
		return Sequence(
				Conjunction(),
				Optional(push(LIST_BEGIN), OneOrMore("or", Conjunction()),
						push(new BNode()), //
						actions.createStmt(peek(), RDF.PROPERTY_TYPE,
								OWL.TYPE_CLASS), //
						actions.createStmt(peek(), OWL.PROPERTY_UNIONOF,
								createRdfList(popList(1, GraphNode.class, 1)))));
	}

	public Rule Entity() {
		return FirstOf(Sequence("Datatype", '(', Datatype(), ')'), //
				Sequence("Class", '(', IriRef(), ')'), //
				Sequence("ObjectProperty", '(', IriRef(), ')'), //
				Sequence("DataProperty", '(', IriRef(), ')'), //
				Sequence("AnnotationProperty", '(', IriRef(), //
						')'), //
				Sequence("NamedIndividual", '(', IriRef(), ')'));
	}

	public Rule Facet() {
		return Sequence(
				FirstOf("length", "minLength", "maxLength", "pattern",
						"langPattern", "<=", '<', ">=", '>'),
				push(createFacet(match().trim())));
	}

	public IriRef createFacet(String facet) {
		String name = facet;
		if ("<=".equals(facet)) {
			name = "minInclusive";
		} else if ("<".equals(facet)) {
			name = "minExclusive";
		} else if (">=".equals(facet)) {
			name = "maxInclusive";
		} else if (">".equals(facet)) {
			name = "maxExclusive";
		}
		return new IriRef(XMLSCHEMA.NAMESPACE + name);
	}

	public Rule Fact(Var<GraphNode> individualIri) {
		Var<Boolean> not = new Var<>(false);
		return Sequence(Optional("not", not.set(true)),
				FirstOf(ObjectPropertyFact(), DataPropertyFact()),
				createPropertyAssertion(individualIri.get(), not.get()));
	}

	public boolean createPropertyAssertion(GraphNode individual,
			boolean negative) {
		if (negative) {
			BNode assertion = new BNode();
			GraphNode property = (GraphNode) pop(1);
			Object value = pop();
			actions.createStmt(assertion, RDF.PROPERTY_TYPE,
					OWL.TYPE_NEGATIVEPROPERTYASSERTION);
			actions.createStmt(assertion, OWL.PROPERTY_SOURCEINDIVIDUAL,
					individual);
			actions.createStmt(assertion, OWL.PROPERTY_ASSERTIONPROPERTY,
					property);
			actions.createStmt(assertion,
					value instanceof Literal ? OWL.PROPERTY_TARGETVALUE
							: OWL.PROPERTY_TARGETINDIVIDUAL, value);
			push(assertion);
		} else {
			actions.createStmt(pop(1), pop(), individual);
			push(individual);
		}
		return true;
	}

	public Rule Frame() {
		return FirstOf(DatatypeFrame(), //
				ClassFrame(), //
				ObjectPropertyFrame(), //
				DataPropertyFrame(), //
				AnnotationPropertyFrame(), //
				IndividualFrame(), //
				Misc() //
		);
	}

	public Rule ImportOntology() {
		return Sequence("Import:", IRI_REF_WS());
	}

	public Rule Individual() {
		return FirstOf(IriRef(), BlankNode());
	}

	public Rule IndividualFrame() {
		Var<GraphNode> individualIri = new Var<>();
		return Sequence(
				"Individual:",
				Individual(),
				individualIri.set((GraphNode) pop()),
				actions.createStmt(individualIri.get(), RDF.PROPERTY_TYPE,
						OWL.TYPE_INDIVIDUAL),
				ZeroOrMore(FirstOf(
						Sequence(
								"Annotations:",
								AnnotatedList(individualIri, null, Annotation())),
						Sequence(
								"Types:",
								AnnotatedList(individualIri, RDF.PROPERTY_TYPE,
										Description())),
						Sequence(
								"Facts:",
								AnnotatedList(individualIri, null,
										Fact(individualIri))),
						Sequence(
								"SameAs:",
								AnnotatedList(individualIri,
										OWL.PROPERTY_SAMEAS, Individual())),
						Sequence(
								"DifferentFrom:",
								AnnotatedList(individualIri,
										OWL.PROPERTY_DIFFERENTFROM,
										Individual())))));
	}

	public Rule InverseObjectProperty() {
		return Sequence("inverse", IriRef());
	}

	// 2.4 Descriptions

	public Rule IRI_REF_WS() {
		return Sequence(IRI_REF(), WS());
	}

	public Rule IriRef() {
		return FirstOf(IRI_REF(), PrefixedName(),
				Sequence(PN_LOCAL(), push(new QName("", (String) pop()))));
	}

	public Rule List(Rule element) {
		return Sequence(push(LIST_BEGIN), element, ZeroOrMore(',', element),
				push(createRdfList(popList(GraphNode.class))));
	}

	public Rule List2(Rule element) {
		return Sequence(push(LIST_BEGIN), element,
				OneOrMore(Sequence(',', element)),
				push(popList(GraphNode.class)));
	}

	// 2.5 Frames and Miscellaneous

	public Rule Literal() {
		return FirstOf(RdfLiteral(), NumericLiteral(), BooleanLiteral());
	}

	public Rule Misc() {
		return FirstOf(
				Sequence("EquivalentClasses:", Annotations(),
						List2(Description())), //
				Sequence("DisjointClasses:", Annotations(),
						List2(Description())), //
				Sequence("EquivalentProperties:", Annotations(),
						List2(IriRef())), //
				Sequence("DisjointProperties:", Annotations(), List2(IriRef())), //
				Sequence("SameIndividual:", Annotations(), List2(Individual())), //
				Sequence("DifferentIndividuals:", Annotations(),
						List2(Individual())));
	}

	public Rule ObjectPropertyCharacteristic() {
		return Sequence(
				FirstOf("Functional", "InverseFunctional", "Reflexive",
						"Irreflexive", "Symmetric", "Asymmetric", "Transitive"),
				push(new IriRef(OWL.NAMESPACE_URI.appendFragment(
						match().trim() + "Property").toString())));
	}

	public Rule ObjectPropertyExpression() {
		return FirstOf(IriRef(), InverseObjectProperty());
	}

	public Rule ObjectPropertyFact() {
		return Sequence(IriRef(), Individual());
	}

	public Rule ObjectPropertyFrame() {
		Var<GraphNode> objectPropertyIri = new Var<>();
		return Sequence(
				"ObjectProperty:",
				IriRef(),
				objectPropertyIri.set((GraphNode) pop()),
				actions.createStmt(objectPropertyIri, RDF.PROPERTY_TYPE,
						OWL.TYPE_OBJECTPROPERTY),
				ZeroOrMore(FirstOf(
						Sequence(
								"Annotations:",
								AnnotatedList(objectPropertyIri, null,
										Annotation())),
						Sequence(
								"Domain:",
								AnnotatedList(objectPropertyIri,
										RDFS.PROPERTY_DOMAIN, Description())),
						Sequence(
								"Range:",
								AnnotatedList(objectPropertyIri,
										RDFS.PROPERTY_RANGE, Description())),
						Sequence(
								"Characteristics:",
								AnnotatedList(objectPropertyIri,
										RDF.PROPERTY_TYPE,
										ObjectPropertyCharacteristic())),
						Sequence(
								"SubPropertyOf:",
								AnnotatedList(objectPropertyIri,
										RDFS.PROPERTY_SUBPROPERTYOF,
										ObjectPropertyExpression())),
						Sequence(
								"EquivalentTo:",
								AnnotatedList(objectPropertyIri,
										OWL.PROPERTY_EQUIVALENTPROPERTY,
										ObjectPropertyExpression())),
						Sequence(
								"DisjointWith:",
								AnnotatedList(objectPropertyIri,
										OWL.PROPERTY_DISJOINTWITH,
										ObjectPropertyExpression())),
						Sequence(
								"InverseOf:",
								AnnotatedList(objectPropertyIri,
										OWL.PROPERTY_INVERSEOF,
										ObjectPropertyExpression())),
						// TODO
						Sequence(
								"SubPropertyChain:",
								WithAnnotations(
										objectPropertyIri,
										OWL.PROPERTY_PROPERTYCHAINAXIOM,
										Sequence(
												push(LIST_BEGIN),
												OneOrMore(Sequence(
														ObjectPropertyExpression(),
														'o',
														ObjectPropertyExpression())),
												push(createRdfList(popList(GraphNode.class)))))) //
				)));
	}

	public Rule Ontology() {
		Var<IriRef> ontologyIri = new Var<>();
		return Sequence(
				"Ontology:",
				Optional(Sequence(
						OntologyIRI(),
						ontologyIri.set((IriRef) pop()),
						actions.createStmt(ontologyIri.get(),
								RDF.PROPERTY_TYPE, OWL.TYPE_ONTOLOGY),
						Optional(Sequence(VersionIRI(), actions.createStmt(
								ontologyIri.get(), OWL.PROPERTY_VERSIONINFO,
								match()))))), //
				ZeroOrMore(ImportOntology()), //

				// FIXME What should be done if ontologyIri is null?

				Annotations(), //
				ZeroOrMore(Frame()));
	}

	public Rule OntologyDocument() {
		return Sequence(ZeroOrMore(PrefixDeclaration()), Ontology(), EOI);
	}

	public Rule OntologyIRI() {
		return IRI_REF_WS();
	}

	public Rule PrefixDeclaration() {
		return Sequence("Prefix:", PNAME_NS(), IRI_REF());
	}

	public Rule Primary() {
		Var<Boolean> isComplement = new Var<>(false);
		return Sequence(
				Optional("not", isComplement.set(true)),
				FirstOf(Restriction(), Atomic()),
				// create complement class
				FirstOf(isComplement.get() //
						&& push(new BNode()) //
						&& actions.createStmt(peek(), RDF.PROPERTY_TYPE,
								OWL.TYPE_CLASS) //
						&& actions.createStmt(peek(),
								OWL.PROPERTY_COMPLEMENTOF, pop(1)), true));
	}

	public Rule Restriction() {
		Var<URI> type = new Var<>();
		Var<URI> on = new Var<>();
		return Sequence(
				FirstOf(Sequence(
						DataPropertyExpression(),
						FirstOf(Sequence("some",
								type.set(OWL.PROPERTY_SOMEVALUESFROM),
								DataPrimary()), //
								Sequence("only",
										type.set(OWL.PROPERTY_ALLVALUESFROM),
										DataPrimary()),
								Sequence("value",
										type.set(OWL.PROPERTY_HASVALUE),
										Literal()), //
								Sequence(
										"min",
										type.set(OWL.PROPERTY_MINCARDINALITY),
										INTEGER(),
										Optional(DataPrimary(), on
												.set(OWL.PROPERTY_ONDATARANGE))), //
								Sequence(
										"max",
										type.set(OWL.PROPERTY_MAXCARDINALITY),
										INTEGER(),
										Optional(DataPrimary(), on
												.set(OWL.PROPERTY_ONDATARANGE))), //
								Sequence(
										"exactly",
										type.set(OWL.PROPERTY_CARDINALITY),
										INTEGER(),
										Optional(DataPrimary(), on
												.set(OWL.PROPERTY_ONDATARANGE))))),
						Sequence(
								ObjectPropertyExpression(),
								FirstOf(Sequence("some",
										type.set(OWL.PROPERTY_SOMEVALUESFROM),
										Primary()),
										Sequence(
												"only",
												type.set(OWL.PROPERTY_ALLVALUESFROM),
												Primary()),
										Sequence(
												"value",
												type.set(OWL.PROPERTY_HASVALUE),
												Individual()),
										Sequence("Self",
												type.set(OWL.PROPERTY_HASSELF),
												push(new BooleanLiteral(true))), //
										Sequence(
												"min",
												type.set(OWL.PROPERTY_MINCARDINALITY),
												INTEGER(),
												Optional(
														Primary(),
														on.set(OWL.PROPERTY_ONCLASS))), //
										Sequence(
												"max",
												type.set(OWL.PROPERTY_MAXCARDINALITY),
												INTEGER(),
												Optional(
														Primary(),
														on.set(OWL.PROPERTY_ONCLASS))), //
										Sequence(
												"exactly",
												type.set(OWL.PROPERTY_CARDINALITY),
												INTEGER(),
												Optional(
														Primary(),
														on.set(OWL.PROPERTY_ONCLASS))))) //
				), createRestriction(type.get(), on.get()));
	}

	public boolean createRestriction(URI type, URI on) {
		Object onTarget = null;
		if (on != null) {
			onTarget = pop();

			if (type.equals(OWL.PROPERTY_CARDINALITY)) {
				type = OWL.PROPERTY_QUALIFIEDCARDINALITY;
			} else if (type.equals(OWL.PROPERTY_MAXCARDINALITY)) {
				type = OWL.PROPERTY_MAXQUALIFIEDCARDINALITY;
			} else if (type.equals(OWL.PROPERTY_MINCARDINALITY)) {
				type = OWL.PROPERTY_MINQUALIFIEDCARDINALITY;
			}
		}
		GraphNode property = (GraphNode) pop(1);
		BNode restriction = new BNode();
		actions.createStmt(restriction, RDF.PROPERTY_TYPE, OWL.TYPE_RESTRICTION);
		actions.createStmt(restriction, OWL.PROPERTY_ONPROPERTY, property);
		actions.createStmt(restriction, type, pop());
		if (on != null) {
			actions.createStmt(restriction, on, onTarget);
		}
		push(restriction);
		return true;
	}

	public Rule RestrictionValue() {
		return Literal();
	}

	public Rule VersionIRI() {
		return IRI_REF_WS();
	}
}