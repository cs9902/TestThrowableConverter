import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.impl.ThrowableFormatOptions;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.core.pattern.ThrowablePatternConverter;
import org.apache.logging.log4j.core.util.Patterns;
import org.apache.logging.log4j.util.Strings;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

@Plugin(name = "ThrowableMaskPatternConvertor", category = PatternConverter.CATEGORY)
@ConverterKeys({ "exMask", "throwableMask", "exceptionMask" })
public class ThrowableMaskPatternConvertor extends ThrowablePatternConverter {

    private String rawOption;

    private List<String> maskExceptions = new ArrayList<String>();

    /**
     * Constructor.
     * @param name Name of converter.
     * @param style CSS style for output.
     * @param options options, may be null.
     * @param config
     */
    protected ThrowableMaskPatternConvertor(final String name, final String style, final String[] options, final Configuration config) {

        super(name, style, options, config);

        if (options != null && options.length > 0) {
            this.rawOption = options[0];
        }

        for (final String oneOption : options) {
            if (oneOption != null) {
                final String option = oneOption.trim();
                if (option.startsWith("filters(") && option.endsWith(")")) {
                    final  String filterStr = option.substring("filters(".length(), option.length() - 1);
                    if (filterStr.length() > 0) {
                        final String[] array = filterStr.split(Patterns.COMMA_SEPARATOR);
                        if (array.length > 0) {
                            for (String oneFilter : array) {
                                if (oneFilter.startsWith("masks(") && oneFilter.endsWith(")")) {
                                    final String maskStr = oneFilter.substring("masks(".length(), oneFilter.length() - 1);
                                    if (maskStr.length() > 0) {
                                        final String[] maskArray = maskStr.split("\\|");
                                        if (maskArray.length > 0) {
                                            for (String mask : maskArray) {
                                                mask = mask.trim();
                                                if (mask.length() > 0) {
                                                    maskExceptions.add(mask);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        List<String> ignorePackages = getOptions().getIgnorePackages();
        for (String onPackage : ignorePackages) {
            if (onPackage.startsWith("masks(") && onPackage.endsWith(")")) {
                ignorePackages.remove(onPackage);
            }
        }

    }

    /**
     * Gets an instance of the class.
     *
     * @param config
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

        if (isSubShortOption()) {
            super.format(event, buffer);
        }
        else if (t != null && options.anyLines()) {
            formatOption(t, getSuffix(event), buffer);
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

    private void formatOption(final Throwable throwable, final String suffix, final StringBuilder buffer) {
        final StringWriter w = new StringWriter();

        throwable.printStackTrace(new PrintWriter(w));
        final int len = buffer.length();
        if (len > 0 && !Character.isWhitespace(buffer.charAt(len - 1))) {
            buffer.append(' ');
        }

        String exceptionName = throwable.getClass().getCanonicalName();
        if (!maskExceptions.contains(exceptionName)) {

            if (!options.allLines() || !Strings.LINE_SEPARATOR.equals(options.getSeparator()) || Strings.isNotBlank(suffix)) {
                final StringBuilder sb = new StringBuilder();
                final String[] array = w.toString().split(Strings.LINE_SEPARATOR);
                final int limit = options.minLines(array.length) - 1;
                final boolean suffixNotBlank = Strings.isNotBlank(suffix);

                for (int i = 0; i <= limit; ++i) {
                    sb.append(array[i]);
                    if (suffixNotBlank) {
                        sb.append(' ');
                        sb.append(suffix);
                    }
                    if (i < limit) {
                        sb.append(options.getSeparator());
                    }
                }
                buffer.append(sb.toString());

            } else {
                buffer.append(w.toString());
            }

        } else {

                final StringBuilder sb = new StringBuilder();
                final String[] array = w.toString().split(Strings.LINE_SEPARATOR);
                final int limit = options.minLines(array.length) - 1;
                final boolean suffixNotBlank = Strings.isNotBlank(suffix);

                sb.append(exceptionName).append(Strings.LINE_SEPARATOR);

                for (int i = 1; i <= limit; ++i) {
                    sb.append(array[i]);
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
    }

}
