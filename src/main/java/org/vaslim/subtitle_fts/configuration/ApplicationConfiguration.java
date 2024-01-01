package org.vaslim.subtitle_fts.configuration;

import fr.noop.subtitle.vtt.VttParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public VttParser vttParser() {
        return new VttParser("utf-8");
    }


}
