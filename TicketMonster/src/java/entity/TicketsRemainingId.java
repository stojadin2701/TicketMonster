package entity;
// Generated Feb 17, 2017 4:46:51 PM by Hibernate Tools 4.3.1



/**
 * TicketsRemainingId generated by hbm2java
 */
public class TicketsRemainingId  implements java.io.Serializable {


     private int eventId;
     private int eventDay;

    public TicketsRemainingId() {
    }

    public TicketsRemainingId(int eventId, int eventDay) {
       this.eventId = eventId;
       this.eventDay = eventDay;
    }
   
    public int getEventId() {
        return this.eventId;
    }
    
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }
    public int getEventDay() {
        return this.eventDay;
    }
    
    public void setEventDay(int eventDay) {
        this.eventDay = eventDay;
    }


   public boolean equals(Object other) {
         if ( (this == other ) ) return true;
		 if ( (other == null ) ) return false;
		 if ( !(other instanceof TicketsRemainingId) ) return false;
		 TicketsRemainingId castOther = ( TicketsRemainingId ) other; 
         
		 return (this.getEventId()==castOther.getEventId())
 && (this.getEventDay()==castOther.getEventDay());
   }
   
   public int hashCode() {
         int result = 17;
         
         result = 37 * result + this.getEventId();
         result = 37 * result + this.getEventDay();
         return result;
   }   


}


