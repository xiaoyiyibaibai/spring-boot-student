/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xiaodonghong.operations;

import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.cache.interceptor.CacheableOperation;
import org.springframework.lang.Nullable;

/**
 * Class describing a cache 'cacheable' operation.
 *
 * @author Costin Leau
 * @author Phillip Webb
 * @author Marcin Kamionowski
 * @since 3.1
 */
public class CustomCacheableOperation extends CacheOperation {

    @Nullable
    private final String unless;

    private final boolean sync;

    private final int refreshTimes;

    private final int ttl;

    public CustomCacheableOperation(CustomCacheableOperation.Builder b) {
        super( b );
        this.unless = b.unless;
        this.sync = b.sync;
        this.refreshTimes = b.refreshTimes;
        this.ttl = b.ttl;
    }

    @Nullable
    public String getUnless() {
        return this.unless;
    }

    public boolean isSync() {
        return this.sync;
    }

    public int getRefreshTimes(){
        return this.refreshTimes;
    }
    public int getTtl(){
        return  this.ttl;
    }

    /**
     * A builder that can be used to create a {@link CacheableOperation}.
     *
     * @since 4.3
     */
    public static class Builder extends CacheOperation.Builder {

        @Nullable
        private String unless;

        private boolean sync;
        private int refreshTimes;
        private int ttl;

        public void setUnless(String unless) {
            this.unless = unless;
        }

        public void setSync(boolean sync) {
            this.sync = sync;
        }

        public void setRefreshTimes(int refreshTimes) {
            this.refreshTimes = refreshTimes;
        }

        public void setTtl(int ttl) {
            this.ttl = ttl;
        }

        @Override
        protected StringBuilder getOperationDescription() {
            StringBuilder sb = super.getOperationDescription();
            sb.append( " | unless='" );
            sb.append( this.unless );
            sb.append( "'" );
            sb.append( " | sync='" );
            sb.append( this.sync );
            sb.append( "'" );
            sb.append( " | refreshTimes='" );
            sb.append( this.refreshTimes );
            sb.append( "'" );
            sb.append( " | ttl='" );
            sb.append( this.ttl );
            sb.append( "'" );
            return sb;
        }

        @Override
        public CustomCacheableOperation build() {
            return new CustomCacheableOperation( this );
        }
    }

}
