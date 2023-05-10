package io.camunda.cherry.joke.api.model;

import java.io.Serializable;

public class JokeResponse implements Serializable {

   private boolean error;
   private String category;
   private String type;
   private String joke;
   private Flags flags;
   private boolean safe;
   private String lang;

   public JokeResponse(){

   }

   public boolean isError() {
      return error;
   }

   public void setError(boolean error) {
      this.error = error;
   }

   public String getCategory() {
      return category;
   }

   public void setCategory(String category) {
      this.category = category;
   }

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getJoke() {
      return joke;
   }

   public void setJoke(String joke) {
      this.joke = joke;
   }

   public Flags getFlags() {
      return flags;
   }

   public void setFlags(Flags flags) {
      this.flags = flags;
   }

   public boolean isSafe() {
      return safe;
   }

   public void setSafe(boolean safe) {
      this.safe = safe;
   }

   public String getLang() {
      return lang;
   }

   public void setLang(String lang) {
      this.lang = lang;
   }
}
