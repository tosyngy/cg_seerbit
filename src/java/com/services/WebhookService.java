/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.services;


import com.entities.Event;
import com.entities.WebhookEvent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.handlers.WebhookHandler;
import com.model.SubscriptionObject;
import com.util.Dao;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * REST Web Service
 *
 * @author centricgateway
 */
@Path("webhook/v1")
public class WebhookService {

    @Context
    private UriInfo context;
    Gson gson = new Gson();
    JsonObject obj ;
    

    Dao dao = new Dao();
    
  //  @Inject
  //  WebhookHandler webhookhandler;

    /**
     * Creates a new instance of GenericResource
     */
 /*   public WebhookService() {
        
    }


    
    @Path("subscribe")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String suscribe(SubscriptionObject subscriptionobj){
        return webhookhandler.subscribe(subscriptionobj);
    }
   */ 
    @Path("transaction/update")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response mysub(WebhookEvent event){
      System.out.println("the event is......."+gson.toJson(event));
      return Response.status(Status.OK).build();
    }
   /*
    @Path("notification")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response notifyMe(Event event){
      System.out.println("the notification going out is......."+gson.toJson(event));
      return Response.status(Status.OK).build();
    }
    
    */
    
}
