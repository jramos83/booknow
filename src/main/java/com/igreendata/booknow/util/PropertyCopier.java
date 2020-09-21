package com.igreendata.booknow.util;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PropertyCopier {
    public void copyProperties(Object from, Object to, Set<String> properties) {
        BeanWrapper srcWrap = PropertyAccessorFactory.forBeanPropertyAccess(from);
        BeanWrapper trgWrap = PropertyAccessorFactory.forBeanPropertyAccess(to);

        properties.forEach(p -> trgWrap.setPropertyValue(p, srcWrap.getPropertyValue(p)));
    }
}
