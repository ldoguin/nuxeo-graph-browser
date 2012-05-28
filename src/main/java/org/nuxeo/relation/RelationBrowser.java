/**
 * 
 */

package org.nuxeo.relation;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.platform.relations.api.Graph;
import org.nuxeo.ecm.platform.relations.api.Node;
import org.nuxeo.ecm.platform.relations.api.QNameResource;
import org.nuxeo.ecm.platform.relations.api.RelationManager;
import org.nuxeo.ecm.platform.relations.api.Resource;
import org.nuxeo.ecm.platform.relations.api.ResourceAdapter;
import org.nuxeo.ecm.platform.relations.api.Statement;
import org.nuxeo.ecm.platform.relations.api.impl.ResourceImpl;
import org.nuxeo.ecm.platform.relations.api.util.RelationConstants;
import org.nuxeo.ecm.platform.relations.api.util.RelationHelper;
import org.nuxeo.ecm.platform.tag.Tag;
import org.nuxeo.ecm.platform.tag.TagService;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.ModuleRoot;
import org.nuxeo.runtime.api.Framework;

/**
 * The root entry for the WebEngine module.
 * 
 * @author ldoguin
 */
@Path("/relationBrowser")
@Produces("text/html;charset=UTF-8")
@WebObject(type = "MyRoot")
public class RelationBrowser extends ModuleRoot {

	@GET
	public Object doGet() {
		return getView("index");
	}

	@GET
	@Path("browse/id/{id}")
	@Produces("text/html;charset=UTF-8")
	public Object getIdBrowser(@PathParam("id") String id) throws Exception {
		return getView("index").arg("id", id);
	}

	@GET
	@Path("browse/predicate/{predicate}")
	@Produces("text/html;charset=UTF-8")
	public Object getPredicateBrowser(@PathParam("predicate") String predicate)
			throws Exception {
		return getView("index").arg("predicate", predicate);
	}

	@GET
	@Path("relation/predicate/{predicate}")
	@Produces("application/json")
	public Object getPredicateSatements(@PathParam("predicate") String predicate)
			throws Exception {
		RelationManager relationManager = Framework
				.getService(RelationManager.class);
		Graph graph = relationManager
				.getGraphByName(RelationConstants.GRAPH_NAME);
		Resource predicateNode = new ResourceImpl(predicate);

		List<Statement> stmts = graph.getStatements(null, predicateNode, null);
		JSONArray statementArray = new JSONArray();
		for (Statement statement : stmts) {
			DocumentModel subjectDoc = getDocumentModel(statement.getSubject(),
					relationManager);
			DocumentModel objectDoc = getDocumentModel(statement.getObject(),
					relationManager);
			String predicateURI = statement.getPredicate().getUri();

			JSONObject statementJSObject = new JSONObject();
			JSONObject subjectJSObject = createDocumentNode(
					subjectDoc.getTitle(), subjectDoc.getId(), "blue");
			JSONObject objectJSObject = createDocumentNode(
					objectDoc.getTitle(), objectDoc.getId(), "blue");
			JSONObject predicateJSObject = createPredicateNode(predicateURI,
					predicateURI);
			statementJSObject.put("subject", subjectJSObject);
			statementJSObject.put("object", objectJSObject);
			statementJSObject.put("predicate", predicateJSObject);
			statementArray.put(statementJSObject);
		}
		return statementArray.toString();
	}

	@GET
	@Path("relation/uuid/{uuid}")
	@Produces("application/json")
	public Object getDocRelations(@PathParam("uuid") String uuid)
			throws Exception {
		RelationManager relationManager = Framework
				.getService(RelationManager.class);
		Graph graph = relationManager
				.getGraphByName(RelationConstants.GRAPH_NAME);
		CoreSession session = ctx.getCoreSession();
		DocumentRef idRef = new IdRef(uuid);
		DocumentModel subjectDoc = session.getDocument(idRef);
		Node subject = RelationHelper.getDocumentResource(subjectDoc);
		JSONObject subjectJSObject = createDocumentNode(subjectDoc.getTitle(),
				subjectDoc.getId(), "blue");
		List<Statement> stmts = graph.getStatements(subject, null, null);
		JSONArray statementArray = new JSONArray();
		for (Statement statement : stmts) {
			DocumentModel objectDoc = getDocumentModel(statement.getObject(),
					relationManager);
			String predicateURI = statement.getPredicate().getUri();
			JSONObject statementJSObject = new JSONObject();
			JSONObject objectJSObject = createDocumentNode(
					objectDoc.getTitle(), objectDoc.getId(), "blue");
			JSONObject predicateJSObject = createPredicateNode(predicateURI,
					predicateURI);
			statementJSObject.put("subject", subjectJSObject);
			statementJSObject.put("object", objectJSObject);
			statementJSObject.put("predicate", predicateJSObject);
			statementArray.put(statementJSObject);
		}
		return statementArray.toString();
	}

	@GET
	@Path("children/{uuid}")
	@Produces("application/json")
	public Object getChildren(@PathParam("uuid") String uuid) throws Exception {
		CoreSession session = ctx.getCoreSession();
		DocumentRef idRef = new IdRef(uuid);
		DocumentModelList children = session.getChildren(idRef);
		DocumentModel parentDoc = session.getDocument(idRef);
		JSONObject subjectJSObject = createDocumentNode(parentDoc.getTitle(),
				parentDoc.getId(), "blue");
		JSONArray statementArray = new JSONArray();
		for (DocumentModel child : children) {
			JSONObject statementJSObject = new JSONObject();
			JSONObject objectJSObject = createDocumentNode(child.getTitle(),
					child.getId(), "blue");
			JSONObject predicateJSObject = createPredicateNode("parentOf",
					"parentOf");
			statementJSObject.put("subject", subjectJSObject);
			statementJSObject.put("object", objectJSObject);
			statementJSObject.put("predicate", predicateJSObject);
			statementArray.put(statementJSObject);
		}
		return statementArray.toString();
	}

	@GET
	@Path("parent/{uuid}")
	@Produces("application/json")
	public Object getParent(@PathParam("uuid") String uuid) throws Exception {
		CoreSession session = ctx.getCoreSession();
		DocumentRef idRef = new IdRef(uuid);
		DocumentModel doc = session.getDocument(idRef);
		DocumentModel parent = session.getParentDocument(idRef);
		JSONArray statementArray = new JSONArray();
		JSONObject statementJSObject = new JSONObject();
		JSONObject subjectJSObject = createDocumentNode(doc.getTitle(), uuid,
				"blue");
		JSONObject objectJSObject = createDocumentNode(parent.getTitle(),
				parent.getId(), "blue");
		JSONObject predicateJSObject = createPredicateNode("childOf", "childOf");
		statementJSObject.put("subject", subjectJSObject);
		statementJSObject.put("object", objectJSObject);
		statementJSObject.put("predicate", predicateJSObject);
		statementArray.put(statementJSObject);
		return statementArray.toString();
	}

	@GET
	@Path("tag/list/{uuid}")
	@Produces("application/json")
	public Object getDocTagList(@PathParam("uuid") String uuid)
			throws Exception {
		CoreSession session = ctx.getCoreSession();
		DocumentRef idRef = new IdRef(uuid);
		DocumentModel doc = session.getDocument(idRef);
		List<Tag> tags = getDocumentTags(doc);
		JSONObject subjectJSObject = createDocumentNode(doc.getTitle(),
				doc.getId(), "blue");
		JSONArray statementArray = new JSONArray();
		for (Tag tag : tags) {
			JSONObject statementJSObject = new JSONObject();
			JSONObject predicateJSObject = createTagNode(tag.getLabel(),
					tag.getWeight());
			statementJSObject.put("subject", subjectJSObject);
			statementJSObject.put("predicate", predicateJSObject);
			statementArray.put(statementJSObject);
		}
		return statementArray.toString();
	}

	@GET
	@Path("tag/documents/{tagLabel}")
	@Produces("application/json")
	public Object getTagDocuments(@PathParam("tagLabel") String tagLabel)
			throws Exception {
		List<String> documentIds = getTagDocumentIds(tagLabel);
		if (documentIds != null && !documentIds.isEmpty()) {
			JSONObject predicateJSObject = createTagNode(tagLabel, 1);
			JSONArray statementArray = new JSONArray();
			for (String id : documentIds) {
				JSONObject statementJSObject = new JSONObject();
				CoreSession session = ctx.getCoreSession();
				DocumentRef idRef = new IdRef(id);
				DocumentModel object = session.getDocument(idRef);
				JSONObject objectJSObject = createDocumentNode(
						object.getTitle(), object.getId(), "blue");
				statementJSObject.put("predicate", predicateJSObject);
				statementJSObject.put("object", objectJSObject);
				statementArray.put(statementJSObject);
			}
			return statementArray.toString();
		}
		return null;
	}

	@GET
	@Path("choices/{uuid}")
	@Produces("application/json")
	public Object getChoices(@PathParam("uuid") String uuid) throws Exception {
		CoreSession session = ctx.getCoreSession();
		DocumentRef idRef = new IdRef(uuid);
		DocumentModel doc = session.getDocument(idRef);
		JSONObject subjectJSObject = createDocumentNode(doc.getTitle(),
				doc.getId(), "blue");
		JSONArray statementArray = new JSONArray();

		// add Parent Navigation
		JSONObject statementJSObject = new JSONObject();
		JSONObject predicateJSObject = createPredicateNode("childOf",
				doc.getId());
		predicateJSObject.put("relation", "childOf");
		statementJSObject.put("subject", subjectJSObject);
		statementJSObject.put("predicate", predicateJSObject);
		statementArray.put(statementJSObject);

		// add Children Navigation
		statementJSObject = new JSONObject();
		predicateJSObject = createPredicateNode("parentOf", doc.getId());
		predicateJSObject.put("relation", "parentOf");
		statementJSObject.put("subject", subjectJSObject);
		statementJSObject.put("predicate", predicateJSObject);
		statementArray.put(statementJSObject);

		// add tags Navigation
		statementJSObject = new JSONObject();
		predicateJSObject = createPredicateNode("hasTags", doc.getId());
		predicateJSObject.put("relation", "hasTags");
		statementJSObject.put("subject", subjectJSObject);
		statementJSObject.put("predicate", predicateJSObject);
		statementArray.put(statementJSObject);

		// add Relations Navigation
		statementJSObject = new JSONObject();
		predicateJSObject = createPredicateNode("hasRelations", doc.getId());
		predicateJSObject.put("relation", "hasRelations");
		statementJSObject.put("subject", subjectJSObject);
		statementJSObject.put("predicate", predicateJSObject);
		statementArray.put(statementJSObject);

		return statementArray.toString();
	}

	public List<String> getTagDocumentIds(String tagLabel)
			throws ClientException {
		List<String> documentIds = getTagService().getTagDocumentIds(
				ctx.getCoreSession(), tagLabel, null);
		return documentIds;
	}

	public List<Tag> getDocumentTags(DocumentModel document)
			throws ClientException {
		String docId = getDocIdForTag(document);
		List<Tag> tags = getTagService().getDocumentTags(ctx.getCoreSession(),
				docId, null);
		Collections.sort(tags, Tag.LABEL_COMPARATOR);
		return tags;
	}

	public static String getDocIdForTag(DocumentModel doc) {
		return doc.isProxy() ? doc.getSourceId() : doc.getId();
	}

	protected TagService getTagService() throws ClientException {
		TagService tagService;
		try {
			tagService = Framework.getService(TagService.class);
		} catch (Exception e) {
			throw new ClientException(e);
		}
		if (tagService == null) {
			return null;
		}
		return tagService.isEnabled() ? tagService : null;
	}

	protected DocumentModel getDocumentModel(Node node,
			RelationManager relationManager) throws ClientException {
		if (node.isQNameResource()) {
			QNameResource resource = (QNameResource) node;
			Map<String, Serializable> context = new HashMap<String, Serializable>();
			context.put(ResourceAdapter.CORE_SESSION_ID_CONTEXT_KEY, ctx
					.getCoreSession().getSessionId());
			Object o = relationManager.getResourceRepresentation(
					resource.getNamespace(), resource, context);
			if (o instanceof DocumentModel) {
				return (DocumentModel) o;
			}
		}
		return null;
	}

	private JSONObject createJsonNode(int mass, String color, String label,
			String type, int size, String uuid) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("mass", mass);
		map.put("color", color);
		map.put("label", label);
		map.put("type", type);
		map.put("size", size);
		map.put("uuid", uuid);
		JSONObject jso = new JSONObject(map);
		return jso;
	}

	private JSONObject createDocumentNode(String label, String uuid,
			String color) throws JSONException {
		JSONObject docNode = createJsonNode(50, color, label, "doc", 70, uuid);
		docNode.put("version", false);
		docNode.put("open", false);
		return docNode;
	}

	private JSONObject createPredicateNode(String label, String uuid)
			throws JSONException {
		JSONObject predicateNode = createJsonNode(1, "grey", label, "virtual",
				48, uuid);
		predicateNode.put("open", false);
		return predicateNode;
	}

	private JSONObject createTagNode(String label, long weight)
			throws JSONException {
		JSONObject predicateNode = createJsonNode(1, "grey", label, "virtual",
				48, label);
		predicateNode.put("relation", "tag");
		predicateNode.put("open", false);
		// predicateNode.put("size", 48 * weight);
		return predicateNode;
	}

	private JSONObject createDeadEnd() throws JSONException {
		JSONObject predicateNode = createJsonNode(1, "black", "Dead End",
				"virtual", 48, "Dead End");
		return predicateNode;
	}

}
