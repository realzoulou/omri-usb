package eu.hradio.core.radiodns.radioepg.programmeinformation;

public enum BroadcastType
{
    BROADCAST_TYPE_ON_AIR("on-air"), 
    BROADCAST_TYPE_OFF_AIR("off-air");
    
    private final String mTypeString;
    
    private BroadcastType(final String tyepString) {
        this.mTypeString = tyepString;
    }
    
    public static BroadcastType fromString(final String typeString) {
        if (typeString.equalsIgnoreCase(BroadcastType.BROADCAST_TYPE_OFF_AIR.mTypeString)) {
            return BroadcastType.BROADCAST_TYPE_OFF_AIR;
        }
        return BroadcastType.BROADCAST_TYPE_ON_AIR;
    }
}
