package com.tatsuyaoiw.rssreader.resource;

import com.tatsuyaoiw.rssreader.json.JsonEntry;
import com.tatsuyaoiw.rssreader.json.JsonFeed;
import com.tatsuyaoiw.rssreader.json.JsonSubscription;
import com.tatsuyaoiw.rssreader.json.request.JsonSubscribeRequest;
import com.tatsuyaoiw.rssreader.model.Entry;
import com.tatsuyaoiw.rssreader.model.Feed;
import com.tatsuyaoiw.rssreader.model.Subscription;
import com.tatsuyaoiw.rssreader.service.SubscriptionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@AllArgsConstructor(onConstructor = @__(@Inject))
@Path("subscriptions")
@Produces(APPLICATION_JSON)
@Singleton
@Slf4j
public class SubscriptionResource {

    private final SubscriptionService subscriptionService;

    @GET
    public Response list() {
        return Response.ok(subscriptionService.list().stream()
                                              .map(SubscriptionResource::toJson)
                                              .collect(toList()))
                       .build();
    }

    @GET
    @Path("{id}")
    public Response get(@PathParam("id") Integer id) {
        return subscriptionService.get(id)
                                  .map(Response::ok)
                                  .orElse(Response.status(404))
                                  .build();
    }

    @POST
    public Response subscribe(@Context UriInfo uriInfo, JsonSubscribeRequest request) {
        return subscriptionService.add(request.getUrl())
                                  .map(it -> Response.created(toUri(uriInfo, it.getId())))
                                  .orElse(Response.status(400))
                                  .build();
    }

    @DELETE
    @Path("{id}")
    public Response unsubscribe(@PathParam("id") Integer id) {
        subscriptionService.delete(id);
        return Response.noContent().build();
    }


    private static JsonSubscription toJson(Subscription input) {
        return JsonSubscription.builder()
                               .id(input.getId())
                               .url(input.getUrl())
                               .feed(toJson(input.getFeed()))
                               .build();
    }

    private static JsonFeed toJson(Feed input) {
        return JsonFeed.builder()
                       .title(input.getTitle())
                       .description(input.getDescription())
                       .entries(input.getEntries().stream()
                                     .map(SubscriptionResource::toJson)
                                     .collect(toList()))
                       .build();
    }

    private static JsonEntry toJson(Entry input) {
        return JsonEntry.builder()
                        .title(input.getTitle())
                        .link(input.getLink())
                        .description(input.getDescription())
                        .build();
    }

    private static URI toUri(UriInfo uriInfo, Integer id) {
        return uriInfo.getBaseUriBuilder()
                      .path("subscriptions")
                      .path(id.toString())
                      .build();
    }
}
