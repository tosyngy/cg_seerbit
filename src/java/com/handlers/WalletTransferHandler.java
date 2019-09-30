/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.handlers;

import com.entities.Transaction;
import com.entities.TransactionEvent;
import com.entities.TransactionInfo;
import com.entities.UserInfo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.model.FetchWalletPayload;
import com.transfer.WalletCreationObject;
import com.transfer.WalletTransactionNotification;
import com.transfer.WalletTransferTransacionRequestProxy;
import com.util.Dao;
import com.util.Utilities;
import com.validator.Validator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author centricgateway
 */
@RequestScoped
public class WalletTransferHandler {
    
    HttpPost post;
    CloseableHttpResponse response;
    CloseableHttpClient client = HttpClients.createDefault();;
    JsonObject obj,obj2;
    JsonObject MerchantObj;
    Gson gson= new Gson();
    StringEntity ent;
    Dao dao = new Dao();
    
    Utilities util = new Utilities();
    ExecutorService executorservice  = Executors.newFixedThreadPool(1);
    
    //default values
      String walletdaysactive="1";
      String walletminutesactive="0";
      String amountcontrol="FIXEDAMOUNT";
    
    
    WalletCreationObject wallet = new WalletCreationObject();
    
   
    
  
     public String initiateTransfer(WalletTransferTransacionRequestProxy payload){
     // needs to generate the wallet number     
     
            try{
                          
                //get the merchantname
                MerchantObj = util.getMerchantInfo(payload.getPublic_key());
                
                    
                    // format the date
           DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy:MM:dd hh:mm:ss");
           String datetime=df.format(LocalDateTime.now());

        
        // Initiate Entities
        com.entities.Transaction trx = new com.entities.Transaction();
        com.entities.TransactionEvent trx_evt = new TransactionEvent();
        com.entities.TransactionInfo trx_info = new TransactionInfo();
        com.entities.UserInfo user_info = new UserInfo();
        
        // Set properties of the entities
           
           trx_info.setAmount(payload.getAmount());
           trx_info.setFee(payload.getFee());
           trx_info.setCallbackurl("not applicable");
           trx_info.setClientAppCode(payload.getClientappcode());
           trx_info.setDatetime(datetime);
           trx_info.setDescription(payload.getDescription());
           trx_info.setPublic_key(payload.getPublic_key());
           trx_info.setRedirecturl("not applicable");
           trx_info.setReference(payload.getTranref());
           trx_info.setChannel("wallet transfer");
           trx_info.setChannelType("transfer");
           trx_info.setNumber("***********");
           trx_info.setTransactionEvent(trx_evt);
           trx_info.setSourceIP(payload.getSourceIP());
           trx_info.setDeviceType(payload.getDeviceType());

           user_info.setCountry(payload.getCountry());
           user_info.setCurrency(payload.getCurrency());
           user_info.setEmail(payload.getEmail());
           user_info.setFullname(payload.getFullname());
           user_info.setMobile(payload.getMobile());
           user_info.setTransactionInfo(trx_info);
           
                                 
           trx.setRef(payload.getTranref());
           trx.setUserinfo(user_info);
           
        
          Validator validator = new Validator();
 
           JsonObject status= dao.addObject(trx);  
          
             if(status.get("code").getAsString().equals("00")){

             String fname=util.getAppProperties().getProperty("settlementaccountfirstname");
             String lname=util.getAppProperties().getProperty("settlementaccountlastname");
             String settlementaccountname=fname+" "+lname;
             
             BigDecimal amount=new BigDecimal(payload.getFee()).add(new BigDecimal(payload.getAmount()));
             wallet.setAmount(amount.toString());
             wallet.setAmountcontrol(amountcontrol);
             wallet.setEmail(payload.getEmail());
             wallet.setExtradata("");
             wallet.setMobilenumber(payload.getMobile());
             wallet.setSettlementaccount(util.getAppProperties().getProperty("settlementaccount"));
             wallet.setSettlementaccountname(settlementaccountname);
             wallet.setWalletdaysactive(walletdaysactive);
             wallet.setWalletminutesactive(walletminutesactive);
             wallet.setWalletname(MerchantObj.get("payload").getAsJsonObject().get("business_name").getAsString());
             
                System.out.println(" ========= wallet creation object ======="+gson.toJson(wallet));
             
                post = new HttpPost(util.getAppProperties().getProperty("create_wallet_endpoint"));
                post.setHeader("x-api-key", util.getAppProperties().getProperty("x-api-key"));
                post.setHeader("Content-Type", "application/json");
                
                ent = new StringEntity(gson.toJson(wallet));
                post.setEntity(ent);
                response=client.execute(post);
                String msg = EntityUtils.toString(response.getEntity());
                obj = new JsonParser().parse(msg).getAsJsonObject();
                System.out.println("response from thirdparty service..."+obj);
                
                
                             if(!obj.isJsonNull()){
                                System.out.println("sending to log now.....");
                                 //Create a thread to log the event 
                                 executorservice.execute(()->{
                                     System.out.println("sending to log the now.....");

                                     // will need to determine the gateway used e.g MPGS,PIN, etc.. when i get the merchant info
                                     String res  = new Dao().nlogTransactionEvent("Transfer", payload.getTranref(), payload.getTranref(), "S20", "Transaction is pending").toString();
                                     System.out.println("response....after log"+res);
                                 });

                                return obj.toString();
                              }else{
                               obj = new JsonObject();
                               obj.addProperty("code", "S7");
                               obj.addProperty("message", "Operation Failed");
                               return obj.toString();
                             }
                                
               }else{
                 System.out.println("-----status----"+status);
               return status.toString();
             }
            }catch(Exception e){
                 obj = new JsonObject();
                 obj.addProperty("code", "S7");
                 obj.addProperty("message", "failed");
                 System.out.println("========error======"+e.getMessage());
                 return obj.toString();
            }
    
    }
    
     
     public String logWalletEvent(WalletTransactionNotification payload){

      //update transactionEvent, transaction table  
      //log wallet notification returned by highstreet
      
        if(payload!=null){
            
            //fetch tranaction with creditacct
            Transaction transaction = dao.ngetTransactionDetails(payload.getCraccount());
            System.out.println("====== loging transaction info====="+gson.toJson(transaction));
            
            //update tranaction and send to settlement
            dao.UpdateAndSendToSettlement(transaction.getRef(), transaction.getRef(), "00", "APPROVED");
            
            
        }else{
            
        }
      
       return "done";
     }
     
    
    
    public String getWallet(FetchWalletPayload payload){
    
            try{
                 obj = new JsonParser().parse(util.getThirdPartyApi()).getAsJsonObject();
                 System.out.println("obj is ...."+obj);
             
                if(!obj.has("access_token")){
                    obj2 = new JsonObject();
                    obj2.addProperty("code", "S7");
                    obj2.addProperty("message", "operation failed");
                    System.out.println("cuase......"+obj);
                   return obj2.toString();
                }
             
                post = new HttpPost(util.getAppProperties().getProperty("fetch_wallet_info"));
                post.setHeader("x-api-key", util.getAppProperties().getProperty("x-api-key"));
                post.setHeader("Content-Type", "application/json");
                
                StringEntity ent = new StringEntity(gson.toJson(payload));
                post.setEntity(ent);
                response=client.execute(post);
                String msg = EntityUtils.toString(response.getEntity());
                obj = new JsonParser().parse(msg).getAsJsonObject();
                System.out.println("response from core..."+obj);
                return obj.toString();
                
            }catch(Exception e){
                 obj = new JsonObject();
                 obj.addProperty("code", "S7");
                 obj.addProperty("message", "failed");
                 System.out.println("========error======"+e.getMessage());
                 return obj.toString();
            }
    
    }
    
    public String generateAccountNumber(){
            Set set = new HashSet();
            Random rand = new Random();
            for(int i=0;i<=7;i++){
              int randomNum = rand.nextInt(9999999);
              if(!set.contains(randomNum)){
                  set.add(randomNum);
                  System.out.println(i + " Random No: " + randomNum);
              }else {
               i--;
              }
            }
         return "done";
    }
    
    
    public String getNameEnquiry(String reference){
      Transaction transaction =  dao.ngetTransactionDetails(reference);
      String acctname=transaction.getUserinfo().getTransactionInfo().getBusinessName()+"-"+transaction.getUserinfo().getFullname();
      obj = new JsonObject();
      obj.addProperty("code", "00");
      obj.addProperty("message", "valid");
      obj.addProperty("accountname",acctname );
      return obj.toString();
    }
    
    public static void main(String[] args) {
        System.out.println(new WalletTransferHandler().generateAccountNumber());
    }
    
}
