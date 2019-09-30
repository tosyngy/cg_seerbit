/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webhook.engine;

import com.entities.Transaction;
import com.entities.WebhookEvent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.util.Dao;
import com.util.DateTimeFormater;
import com.util.Utilities;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author centricgateway
 */
public class Handler implements Runnable{
    
    CloseableHttpClient client = HttpClients.createDefault();
    HttpPost post;
    HttpGet get;
    CloseableHttpResponse response;
    Gson gson = new Gson();
    JsonObject obj;
    JsonObject obj2;
    Dao dao = new Dao();
    Utilities util = new Utilities();
    
    public List getAllPendingTransactions(){
     List transactionList = dao.ngetUndeterminTransactions();
      return transactionList;
    }
    
    
    public void performOperations(JsonObject obj){
    
         ExecutorService service = Executors.newFixedThreadPool(1);
         List<Transaction> transactionList = getAllPendingTransactions();
         
         for(Transaction transaction: transactionList){
              
         }
         
         service.execute(()->{
             
         });
         
    
    }
    
    public String fetchTransactionStatus(Transaction transaction){
    
      return "";
    }
    
    
    public String compareStatusWithDbStatus(){
    
      return "";
    }
    
    
    public void fetchStatusInfo(){
    
    
    }
    

    @Override
    public void run() {
      List <Transaction> list =  dao.ngetUndeterminTransactions();
      // Iterate over each entity
      for(Transaction transaction : list){
          
          // fetch transaction status from core
       String data   =   util.fetchStatusFromCore(transaction.getRef());
          
          
      }
    }
    
    
    public void sendNotification(String reference){
    
          try{
              
              // Construct Object to be sent  
             Transaction transaction =  dao.ngetTransactionDetails(reference);
            
              WebhookEvent event = new WebhookEvent();
              event.setGeneratedAt(DateTimeFormater.getRealTimeFormat());
              event.setAmount(transaction.getUserinfo().getTransactionInfo().getAmount());
              event.setMobile(transaction.getUserinfo().getMobile());
              event.setName(transaction.getUserinfo().getFullname());
              event.setReference(transaction.getRef());
              event.setStatus(transaction.getStatus());
              
              //fetch merchant webhook url from User-Management System
              obj = util.fectchMerchantInfo(transaction.getUserinfo().getTransactionInfo().getPublic_key());
              String endpoint =obj.get("weebhook").getAsJsonObject().get("url").getAsString();
               
              
              //save copy on the system
             String id = dao.add(event);
             Long entityId=Long.parseLong(id);
              
             // prepare to send to third party              
             post = new HttpPost(endpoint);
             post.setHeader("Content-Type", "application/json");
             StringEntity ent = new StringEntity(gson.toJson(event));
             post.setEntity(ent);
             response=client.execute(post);

             
             // check and log the response from thirdpart 
             int data=response.getStatusLine().getStatusCode();
             
             // updating the WebhookEvent
             dao.nupdateWebhookEvent(Integer.toString(data), entityId);
             System.out.println("==== response===="+data);

           
         }catch(Exception e){
           obj = new JsonObject();
           obj.addProperty("code", "S23");
           obj.addProperty("message", "operation failed");
           System.out.println("cause...."+e.getMessage());
           
         }
    
    }
    
    
    
    public void test(){
        System.out.println(util.fetchStatusFromCore("F621625411565007577168"));    
    }
    
    
}
