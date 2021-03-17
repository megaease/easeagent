/*
 * Copyright (c) 2017, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent;

import com.megaease.easeagent.gen.Assembly;
import com.megaease.easeagent.log4j2.PostAppender;
import com.megaease.easeagent.sniffer.*;
import com.megaease.easeagent.sniffer.jedis.v3.JedisAdvice;
import com.megaease.easeagent.sniffer.lettuce.v5.advice.*;

@Assembly({
//        CaptureTrace.class
//        , CaptureExecuteSql.class
//        , CaptureHttpRequest.class
//        , TraceHttpServlet.class
//        , TraceHttpClient.class
//        , TraceRestTemplate.class
//        , TraceJedis.class
//        , TraceJdbcStatement.class
//        , MeasureJdbcStatement.class
//        , MeasureJdbcGetConnection.class
//        , MeasureHttpRequest.class
//        , CaptureCaller.class
//        , HttpServletAdvice.class
        HttpFilterAdvice.class,
        SpringGatewayHttpHeadersFilterAdvice.class,
        SpringGatewayInitGlobalFilterAdvice.class,
        RestTemplateAdvice.class,
        FeignClientAdvice.class,
        JdbcDataSourceAdvice.class,
        JdbcStatementAdvice.class,
        ServiceNamePropagationAdvice.class,
        StatefulRedisConnectionAdvice.class,
        RedisClientAdvice.class,
        RedisChannelWriterAdvice.class,
        JedisAdvice.class,


})
abstract class Easeagent {
    // This static code is to keep the links of the dependencies for shade plugin, not for real runtime.
    static {
        System.out.println(PostAppender.class);
        System.out.println(org.apache.logging.log4j.core.appender.AppenderSet.class);
        System.out.println(org.apache.logging.log4j.core.appender.CountingNoOpAppender.class);
//        System.out.println(org.apache.logging.log4j.core.appender.db.ColumnMapping.class);
//        System.out.println(org.apache.logging.log4j.core.appender.db.jdbc.ColumnConfig.class);
//        System.out.println(org.apache.logging.log4j.core.appender.db.jdbc.DataSourceConnectionSource.class);
//        System.out.println(org.apache.logging.log4j.core.appender.db.jdbc.FactoryMethodConnectionSource.class);
//        System.out.println(org.apache.logging.log4j.core.appender.db.jdbc.JdbcAppender.class);
//        System.out.println(org.apache.logging.log4j.core.appender.db.jpa.JpaAppender.class);
        System.out.println(org.apache.logging.log4j.core.appender.FailoverAppender.class);
        System.out.println(org.apache.logging.log4j.core.appender.FailoversPlugin.class);
        System.out.println(org.apache.logging.log4j.core.appender.FileAppender.class);
        System.out.println(org.apache.logging.log4j.core.appender.MemoryMappedFileAppender.class);
//        System.out.println(org.apache.logging.log4j.core.appender.mom.jeromq.JeroMqAppender.class);
//        System.out.println(org.apache.logging.log4j.core.appender.mom.JmsAppender.class);
//        System.out.println(org.apache.logging.log4j.core.appender.mom.JmsAppender.class);
//        System.out.println(org.apache.logging.log4j.core.appender.mom.JmsAppender.class);
//        System.out.println(org.apache.logging.log4j.core.appender.mom.kafka.KafkaAppender.class);
        System.out.println(org.apache.logging.log4j.core.appender.NullAppender.class);
        System.out.println(org.apache.logging.log4j.core.appender.OutputStreamAppender.class);
        System.out.println(org.apache.logging.log4j.core.appender.RandomAccessFileAppender.class);
        System.out.println(org.apache.logging.log4j.core.appender.rewrite.LoggerNameLevelRewritePolicy.class);
        System.out.println(org.apache.logging.log4j.core.appender.rewrite.MapRewritePolicy.class);
        System.out.println(org.apache.logging.log4j.core.appender.rewrite.PropertiesRewritePolicy.class);
        System.out.println(org.apache.logging.log4j.core.appender.rewrite.RewriteAppender.class);
        System.out.println(org.apache.logging.log4j.core.appender.rolling.action.DeleteAction.class);
        System.out.println(org.apache.logging.log4j.core.appender.rolling.action.IfAccumulatedFileCount.class);
        System.out.println(org.apache.logging.log4j.core.appender.rolling.action.IfAccumulatedFileSize.class);
        System.out.println(org.apache.logging.log4j.core.appender.rolling.action.IfAll.class);
        System.out.println(org.apache.logging.log4j.core.appender.rolling.action.IfAny.class);
        System.out.println(org.apache.logging.log4j.core.appender.rolling.action.IfFileName.class);
        System.out.println(org.apache.logging.log4j.core.appender.rolling.action.IfLastModified.class);
        System.out.println(org.apache.logging.log4j.core.appender.rolling.action.IfNot.class);
        System.out.println(org.apache.logging.log4j.core.appender.rolling.action.PathSortByModificationTime.class);
        System.out.println(org.apache.logging.log4j.core.appender.rolling.action.ScriptCondition.class);
        System.out.println(org.apache.logging.log4j.core.appender.rolling.CompositeTriggeringPolicy.class);
        System.out.println(org.apache.logging.log4j.core.appender.rolling.CronTriggeringPolicy.class);
        System.out.println(org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy.class);
        System.out.println(org.apache.logging.log4j.core.appender.rolling.DirectWriteRolloverStrategy.class);
        System.out.println(org.apache.logging.log4j.core.appender.RollingFileAppender.class);
        System.out.println(org.apache.logging.log4j.core.appender.rolling.OnStartupTriggeringPolicy.class);
        System.out.println(org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender.class);
        System.out.println(org.apache.logging.log4j.core.appender.rolling.SizeBasedTriggeringPolicy.class);
        System.out.println(org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy.class);
        System.out.println(org.apache.logging.log4j.core.appender.routing.IdlePurgePolicy.class);
        System.out.println(org.apache.logging.log4j.core.appender.routing.Route.class);
        System.out.println(org.apache.logging.log4j.core.appender.routing.Routes.class);
        System.out.println(org.apache.logging.log4j.core.appender.routing.RoutingAppender.class);
        System.out.println(org.apache.logging.log4j.core.appender.ScriptAppenderSelector.class);
//        System.out.println(org.apache.logging.log4j.core.appender.SmtpAppender.class);
        System.out.println(org.apache.logging.log4j.core.appender.SocketAppender.class);
//        System.out.println(org.apache.logging.log4j.core.appender.SyslogAppender.class);
        System.out.println(org.apache.logging.log4j.core.appender.WriterAppender.class);
        System.out.println(org.apache.logging.log4j.core.async.DisruptorBlockingQueueFactory.class);
        System.out.println(org.apache.logging.log4j.core.async.JCToolsBlockingQueueFactory.class);
        System.out.println(org.apache.logging.log4j.core.async.LinkedTransferQueueFactory.class);
        System.out.println(org.apache.logging.log4j.core.config.AppendersPlugin.class);
        System.out.println(org.apache.logging.log4j.core.config.json.JsonConfigurationFactory.class);
        System.out.println(org.apache.logging.log4j.core.config.LoggersPlugin.class);
        System.out.println(org.apache.logging.log4j.core.config.PropertiesPlugin.class);
        System.out.println(org.apache.logging.log4j.core.config.properties.PropertiesConfigurationFactory.class);
        System.out.println(org.apache.logging.log4j.core.config.ScriptsPlugin.class);
        System.out.println(org.apache.logging.log4j.core.config.xml.XmlConfigurationFactory.class);
//        System.out.println(org.apache.logging.log4j.core.config.yaml.YamlConfigurationFactory.class);
//        System.out.println(org.apache.logging.log4j.core.filter.BurstFilter.class);
//        System.out.println(org.apache.logging.log4j.core.filter.DynamicThresholdFilter.class);
//        System.out.println(org.apache.logging.log4j.core.filter.LevelRangeFilter.class);
//        System.out.println(org.apache.logging.log4j.core.filter.MapFilter.class);
//        System.out.println(org.apache.logging.log4j.core.filter.MarkerFilter.class);
//        System.out.println(org.apache.logging.log4j.core.filter.RegexFilter.class);
//        System.out.println(org.apache.logging.log4j.core.filter.ScriptFilter.class);
//        System.out.println(org.apache.logging.log4j.core.filter.StructuredDataFilter.class);
//        System.out.println(org.apache.logging.log4j.core.filter.ThreadContextMapFilter.class);
//        System.out.println(org.apache.logging.log4j.core.filter.ThreadContextMapFilter.class);
//        System.out.println(org.apache.logging.log4j.core.filter.ThresholdFilter.class);
//        System.out.println(org.apache.logging.log4j.core.filter.TimeFilter.class);
//        System.out.println(org.apache.logging.log4j.core.layout.CsvLogEventLayout.class);
//        System.out.println(org.apache.logging.log4j.core.layout.CsvParameterLayout.class);
//        System.out.println(org.apache.logging.log4j.core.layout.GelfLayout.class);
//        System.out.println(org.apache.logging.log4j.core.layout.HtmlLayout.class);
//        System.out.println(org.apache.logging.log4j.core.layout.JsonLayout.class);
        System.out.println(org.apache.logging.log4j.core.layout.LoggerFields.class);
        System.out.println(org.apache.logging.log4j.core.layout.MarkerPatternSelector.class);
        System.out.println(org.apache.logging.log4j.core.layout.PatternMatch.class);
//        System.out.println(org.apache.logging.log4j.core.layout.Rfc5424Layout.class);
        System.out.println(org.apache.logging.log4j.core.layout.ScriptPatternSelector.class);
//        System.out.println(org.apache.logging.log4j.core.layout.SerializedLayout.class);
//        System.out.println(org.apache.logging.log4j.core.layout.SyslogLayout.class);
//        System.out.println(org.apache.logging.log4j.core.layout.XmlLayout.class);
//        System.out.println(org.apache.logging.log4j.core.layout.YamlLayout.class);
//        System.out.println(org.apache.logging.log4j.core.lookup.JmxRuntimeInputArgumentsLookup.class);
//        System.out.println(org.apache.logging.log4j.core.lookup.JndiLookup.class);
        System.out.println(org.apache.logging.log4j.core.lookup.ResourceBundleLookup.class);
        System.out.println(org.apache.logging.log4j.core.lookup.StructuredDataLookup.class);
        System.out.println(org.apache.logging.log4j.core.net.MulticastDnsAdvertiser.class);
        System.out.println(org.apache.logging.log4j.core.net.SocketAddress.class);
        System.out.println(org.apache.logging.log4j.core.net.SocketOptions.class);
        System.out.println(org.apache.logging.log4j.core.net.SocketPerformancePreferences.class);
        System.out.println(org.apache.logging.log4j.core.net.ssl.KeyStoreConfiguration.class);
        System.out.println(org.apache.logging.log4j.core.net.ssl.SslConfiguration.class);
        System.out.println(org.apache.logging.log4j.core.net.ssl.TrustStoreConfiguration.class);
        System.out.println(org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter.Black.class);
        System.out.println(org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter.Blue.class);
        System.out.println(org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter.Cyan.class);
        System.out.println(org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter.Green.class);
        System.out.println(org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter.Magenta.class);
        System.out.println(org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter.Red.class);
        System.out.println(org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter.White.class);
        System.out.println(org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter.Yellow.class);
        System.out.println(org.apache.logging.log4j.core.pattern.ClassNamePatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.DatePatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.EncodingPatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.EqualsIgnoreCaseReplacementConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.EqualsReplacementConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.FileDatePatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.FileLocationPatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.FullLocationPatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.HighlightConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.IntegerPatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.LevelPatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.LineLocationPatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.LineSeparatorPatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.LoggerPatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.MapPatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.MarkerPatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.MarkerSimpleNamePatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.MaxLengthConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.MdcPatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.MessagePatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.MethodLocationPatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.NdcPatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.RegexReplacementConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.RelativeTimePatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.RootThrowablePatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.SequenceNumberPatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.StyleConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.ThreadIdPatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.ThreadNamePatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.ThreadPriorityPatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.UuidPatternConverter.class);
        System.out.println(org.apache.logging.log4j.core.pattern.VariablesNotEmptyReplacementConverter.class);
        System.out.println(org.apache.logging.log4j.core.script.Script.class);
        System.out.println(org.apache.logging.log4j.core.util.KeyValuePair.class);
    }
}
