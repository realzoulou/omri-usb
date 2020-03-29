package eu.hradio.core.radiodns;

public enum RadioWebApplicationControl
{
    APPLICATION_CONTROL_UNDEFINED, 
    APPLICATION_CONTROL_AUTOSTART, 
    APPLICATION_CONTROL_ENABLED, 
    APPLICATION_CONTROL_DISABLED;
    
    public static RadioWebApplicationControl getControl(final String controlString) {
        if (controlString != null) {
            if (controlString.trim().equalsIgnoreCase("autostart")) {
                return RadioWebApplicationControl.APPLICATION_CONTROL_AUTOSTART;
            }
            if (controlString.trim().equalsIgnoreCase("enabled")) {
                return RadioWebApplicationControl.APPLICATION_CONTROL_ENABLED;
            }
            if (controlString.trim().equalsIgnoreCase("disabled")) {
                return RadioWebApplicationControl.APPLICATION_CONTROL_DISABLED;
            }
        }
        return RadioWebApplicationControl.APPLICATION_CONTROL_UNDEFINED;
    }
}
