package miron.gaskov.result;

public final class ResultTimeParser {
    private ResultTimeParser() {
    }

    public static Double parseToSeconds(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        if (v.isEmpty()) {
            return null;
        }

        v = v.replace(',', '.');

        try {
            if (v.contains(":")) {
                String[] parts = v.split(":");
                if (parts.length < 2 || parts.length > 3) {
                    return null;
                }

                int hours = 0;
                int minutes;
                double seconds;

                if (parts.length == 3) {
                    hours = Integer.parseInt(parts[0].trim());
                    minutes = Integer.parseInt(parts[1].trim());
                    seconds = Double.parseDouble(parts[2].trim());
                } else { // parts.length == 2
                    minutes = Integer.parseInt(parts[0].trim());
                    seconds = Double.parseDouble(parts[1].trim());
                }

                if (hours < 0 || minutes < 0 || seconds < 0.0) {
                    return null;
                }

                if (minutes >= 60 || seconds >= 60.0) {
                    return null;
                }

                return hours * 3600.0 + minutes * 60.0 + seconds;
            } else {
                double seconds = Double.parseDouble(v);
                if (seconds < 0.0) {
                    return null;
                }
                return seconds;
            }
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
