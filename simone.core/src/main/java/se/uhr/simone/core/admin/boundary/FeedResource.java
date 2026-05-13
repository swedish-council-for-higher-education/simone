package se.uhr.simone.core.admin.boundary;

import java.util.Objects;
import java.util.UUID;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import se.uhr.simone.atom.feed.server.entity.AtomCategory;
import se.uhr.simone.atom.feed.server.entity.AtomEntry;
import se.uhr.simone.common.HttpConstants;
import se.uhr.simone.common.feed.AtomFeedEventRepresentation;
import se.uhr.simone.core.SimOne;
import se.uhr.simone.core.admin.control.ManagedFeedResponse;

@Tag(name = "admin")
public class FeedResource {

	private final SimOne simone;

	private final ManagedFeedResponse simulatedResponse;

	public FeedResource(SimOne simone, ManagedFeedResponse simulatedFeedResponse) {
		this.simone = simone;
		this.simulatedResponse = simulatedFeedResponse;
	}

	@Consumes(MediaType.TEXT_PLAIN)
	@Operation(summary = "Answer with specified code for all feed requests", description = "Enters a state where all feed requests are answered with the specified status code")
	@PUT
	@Path("/response/code")
	public Response setGlobalCode(
			@RequestBody(name = "The HTTP status code", required = true, content = @Content(schema = @Schema(type = SchemaType.INTEGER), example = "401")) int statusCode) {
		simulatedResponse.setGlobalCode(statusCode);
		return Response.ok().build();
	}

	@Operation(summary = "Answer normally for all feed requests", description = "Resumes normal state")
	@DELETE
	@Path("/response/code")
	public Response resetGlobalCode() {
		simulatedResponse.setGlobalCode(ManagedFeedResponse.NORMAL_STATUS_CODE);
		return Response.ok().build();
	}

	@Operation(summary = "Block feed events", description = "Enters a state where no feed event are created")
	@PUT
	@Path("/block")
	public Response blockFeed() {
		simone.getFeedPublisher().setBlocked(true);
		return Response.ok().build();
	}

	@Operation(summary = "Unblock feed events", description = "Resumes normal state")
	@DELETE
	@Path("/block")
	public Response unblockFeed() {
		simone.getFeedPublisher().setBlocked(false);
		return Response.ok().build();
	}

	@Consumes(MediaType.TEXT_PLAIN)
	@Operation(summary = "Delay feed requests", description = "Delay each feed request with the specified time, set 0 to resume to normal")
	@PUT
	@Path("/response/delay")
	public Response setDelay(
			@RequestBody(name = "Time in seconds", required = true, content = @Content(schema = @Schema(type = SchemaType.INTEGER), example = "401")) int timeInSeconds) {
		simulatedResponse.setDelay(timeInSeconds);
		return Response.ok().build();
	}

	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Operation(summary = "Create a custom feed event")
	@APIResponse(responseCode = "200", headers = @Header(name = "eventId", required = true, description = "The id of the new event", schema = @Schema(type = SchemaType.STRING)))
	@POST
	@Path("/event")
	public Response publishEvent(AtomFeedEventRepresentation event) {
		String uid = UUID.randomUUID().toString();

		Long nextSortOrder = simone.getFeedRepository().getNextSortOrder();
		AtomEntry.Build builder = AtomEntry.builder()
				.withAtomEntryId(uid)
				.withSortOrder(nextSortOrder)

				.withSubmittedNow()
				.withContent(se.uhr.simone.atom.feed.server.entity.Content.builder()
						.withValue(event.getContent())
						.withContentType(event.getContentType())
						.build());

		for (se.uhr.simone.common.feed.AtomCategoryRepresentation category : event.getCategorys()) {
			AtomCategory.Build categoryBuilder = AtomCategory.builder().withTerm(AtomCategory.Term.of(category.getTerm()));
			if (Objects.nonNull(category.getLabel())) {
				categoryBuilder.withLabel(AtomCategory.Label.of(category.getLabel()));
			}
			builder.withCategory(categoryBuilder.build());
		}

		simone.getFeedRepository().saveAtomEntry(builder.build());

		return Response.status(Status.OK).header(HttpConstants.EVENT_ID_HEADER, uid).build();
	}
}
