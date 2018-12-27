package com.kubeman.common.logs;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.impl.ThrowableFormatOptions;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.core.pattern.ThrowablePatternConverter;
import org.apache.logging.log4j.util.Strings;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

@Plugin(name = "com.kubeman.common.logs.ThrowableMaskPatternConvertor", category = PatternConverter.CATEGORY)
@ConverterKeys({"exMask", "throwableMask", "exceptionMask"})
public class ThrowableMaskPatternConvertor extends ThrowablePatternConverter {

    private String rawOption;

    /**
     * Constructor.
     *
     * @param name    Name of converter.
     * @param style   CSS style for output.
     * @param options options, may be null.
     * @param config config
     */
    private ThrowableMaskPatternConvertor(final String name, final String style, final String[] options, final Configuration config) {

        super(name, style, options, config);

        if (options != null && options.length > 0) {
            this.rawOption = options[0];
        }

    }

    /**
     * Gets an instance of the class.
     *
     * @param config config
     * @param options pattern options, may be null.  If first element is "short",
     *                only the first line of the throwable will be formatted.
     * @return instance of class.
     */
    public static ThrowableMaskPatternConvertor newInstance(final Configuration config, final String[] options) {
        return new ThrowableMaskPatternConvertor("Throwable", "throwable", options, config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final StringBuilder buffer) {
        final Throwable t = event.getThrown();

        List<String> exceptionMasks = getOptions().getIgnorePackages();
        if (exceptionMasks == null || exceptionMasks.isEmpty()) {
            super.format(event, buffer);
        } else {
            if (isSubShortOption()) {
                super.format(event, buffer);
            } else if (t != null && options.anyLines()) {
                formatExceptionMasksOption(t, getSuffix(event), buffer);
            }
        }


    }

    private boolean isSubShortOption() {
        return ThrowableFormatOptions.MESSAGE.equalsIgnoreCase(rawOption) ||
                ThrowableFormatOptions.LOCALIZED_MESSAGE.equalsIgnoreCase(rawOption) ||
                ThrowableFormatOptions.FILE_NAME.equalsIgnoreCase(rawOption) ||
                ThrowableFormatOptions.LINE_NUMBER.equalsIgnoreCase(rawOption) ||
                ThrowableFormatOptions.METHOD_NAME.equalsIgnoreCase(rawOption) ||
                ThrowableFormatOptions.CLASS_NAME.equalsIgnoreCase(rawOption);
    }

    private void formatExceptionMasksOption(final Throwable throwable, final String suffix, final StringBuilder buffer) {
        final StringWriter w = new StringWriter();

        throwable.printStackTrace(new PrintWriter(w));
        final int len = buffer.length();
        if (len > 0 && !Character.isWhitespace(buffer.charAt(len - 1))) {
            buffer.append(' ');
        }

        final StringBuilder sb = new StringBuilder();
        final String[] array = w.toString().split(Strings.LINE_SEPARATOR);
        final int limit = options.minLines(array.length) - 1;
        final boolean suffixNotBlank = Strings.isNotBlank(suffix);

        for (int i = 0; i <= limit; ++i) {

            if (!isMaskException(array[i], sb)) {
                sb.append(array[i]);
            }
            if (suffixNotBlank) {
                sb.append(' ');
                sb.append(suffix);
            }
            if (i < limit) {
                sb.append(options.getSeparator());
            }
        }
        buffer.append(sb.toString());

    }

    private boolean isMaskException(String logs, StringBuilder sb) {
        List<String> exceptionMasks = getOptions().getIgnorePackages();
        if (exceptionMasks == null || exceptionMasks.isEmpty()) {
            return false;
        } else {
            for (String oneMask : exceptionMasks) {
                String indicator = oneMask + ":";
                if (logs.contains(indicator)) {
                    int position = logs.indexOf(indicator) + indicator.length() - 1;
                    sb.append(logs, 0, position);
                    return true;
                }
            }
        }
        return false;
    }


}
