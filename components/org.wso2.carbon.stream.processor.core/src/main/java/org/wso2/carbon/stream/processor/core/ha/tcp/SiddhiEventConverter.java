/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.stream.processor.core.ha.tcp;


import org.apache.log4j.Logger;
import org.wso2.carbon.stream.processor.core.event.queue.EventQueueManager;
import org.wso2.carbon.stream.processor.core.event.queue.QueuedEvent;
import org.wso2.carbon.stream.processor.core.util.BinaryMessageConverterUtil;
import org.wso2.siddhi.core.event.Event;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * This class is a implementation EventConverter to create the event from the Binary message.
 * This is used within data bridge to create the event from the row message received.
 */
public class SiddhiEventConverter {
    static final Logger LOG = Logger.getLogger(SiddhiEventConverter.class);
    private static int count = 0;

    public static Event[] toConvertAndEnqueue(ByteBuffer messageBuffer, EventQueueManager eventQueueManager) {
        try {
            String sourceHandlerElementId;
            int stringSize = messageBuffer.getInt();
            if (stringSize == 0) {
                sourceHandlerElementId = null;
            } else {
                sourceHandlerElementId = BinaryMessageConverterUtil.getString(messageBuffer, stringSize);
            }

            String attributes;
            stringSize = messageBuffer.getInt();
            if (stringSize == 0) {
                attributes = null;
            } else {
                attributes = BinaryMessageConverterUtil.getString(messageBuffer, stringSize);
            }
            String[] attributeTypes = attributes.substring(1, attributes.length() - 1).split(", ");
            //int numberOfEvents = messageBuffer.getInt();

            Event[] events = new Event[0];
            //for (int i = 0; i < numberOfEvents; i++) {
            events = new Event[]{getEvent(messageBuffer, attributeTypes)};
                //eventQueueManager.addToQueue();
            //}
            count++;
            LOG.info("RECEIVED EVENT - " + sourceHandlerElementId + "       ||      " + events[0].toString() + "    |   COUNT " + count);
            return events;
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    static Event getEvent(ByteBuffer byteBuffer, String[] attributeTypes) throws UnsupportedEncodingException {
        Event event = new Event();
        long timeStamp = byteBuffer.getLong();
        event.setTimestamp(timeStamp);
        event.setData(toObjectArray(byteBuffer, attributeTypes));
        return event;
    }

    static Object[] toObjectArray(ByteBuffer byteBuffer,
                                  String[] attributeTypeOrder) throws UnsupportedEncodingException {
        if (attributeTypeOrder != null) {
            Object[] objects = new Object[attributeTypeOrder.length];
            for (int i = 0; i < attributeTypeOrder.length; i++) {
                switch (attributeTypeOrder[i]) {
                    case "INT":
                        objects[i] = byteBuffer.getInt();
                        break;
                    case "LONG":
                        objects[i] = byteBuffer.getLong();
                        break;
                    case "STRING":
                        int stringSize = byteBuffer.getInt();
                        if (stringSize == 0) {
                            objects[i] = null;
                        } else {
                            objects[i] = BinaryMessageConverterUtil.getString(byteBuffer, stringSize);
                        }
                        break;
                    case "DOUBLE":
                        objects[i] = byteBuffer.getDouble();
                        break;
                    case "FLOAT":
                        objects[i] = byteBuffer.getFloat();
                        break;
                    case "BOOL":
                        objects[i] = byteBuffer.get() == 1;
                        break;
                    default:
                        // will not occur
                }
            }
            return objects;
        } else {
            return null;
        }
    }

}
