/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.runtime.spring;

import java.io.IOException;

import org.apache.isis.config.registry.IsisBeanTypeRegistry;
import org.apache.isis.config.registry.TypeMetaData;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import lombok.Getter;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BeanScanInterceptorForSpring implements TypeFilter {
	
	@Getter(lazy=true) private final IsisBeanTypeRegistry typeRegistry = IsisBeanTypeRegistry.current();

	@Override
	public boolean match(
			MetadataReader metadataReader, 
			MetadataReaderFactory metadataReaderFactory)
			throws IOException {
		
		val classMetadata = metadataReader.getClassMetadata();
		if(!classMetadata.isConcrete()) {
			return false;
		}
		
		if(log.isDebugEnabled()) log.debug("scanning concrete type {}", classMetadata.getClassName());
		
		val annotationMetadata = metadataReader.getAnnotationMetadata();
		val annotationTypes = annotationMetadata.getAnnotationTypes();
		val typeMetaData = TypeMetaData.of(classMetadata.getClassName(), annotationTypes);
		
		return getTypeRegistry().isIoCManagedType(typeMetaData);
	}

}
