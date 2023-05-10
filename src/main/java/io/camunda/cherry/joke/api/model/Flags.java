package io.camunda.cherry.joke.api.model;

import java.io.Serializable;

public class Flags implements Serializable {

   private boolean nsfw;
   private boolean religious;
   private boolean political;
   private boolean racist;
   private boolean sexist;
   private boolean explicit;

   public Flags() {

   }

   public boolean isNsfw() {
      return nsfw;
   }

   public void setNsfw(boolean nsfw) {
      this.nsfw = nsfw;
   }

   public boolean isReligious() {
      return religious;
   }

   public void setReligious(boolean religious) {
      this.religious = religious;
   }

   public boolean isPolitical() {
      return political;
   }

   public void setPolitical(boolean political) {
      this.political = political;
   }

   public boolean isRacist() {
      return racist;
   }

   public void setRacist(boolean racist) {
      this.racist = racist;
   }

   public boolean isSexist() {
      return sexist;
   }

   public void setSexist(boolean sexist) {
      this.sexist = sexist;
   }

   public boolean isExplicit() {
      return explicit;
   }

   public void setExplicit(boolean explicit) {
      this.explicit = explicit;
   }
}
