/*
 * The MIT License
 *
 * Copyright (c) 2010, InfraDNA, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.sauce_ondemand;

import com.saucelabs.ci.Browser;
import com.saucelabs.common.SauceOnDemandAuthentication;
import hudson.matrix.Axis;
import hudson.util.Secret;

import java.util.List;
import java.util.Map;

/**
 * {@link Axis} that configures {@code SELENIUM_DRIVER}.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class BrowserAxis extends Axis {

    public BrowserAxis(List<String> values) {
        super("SELENIUM_DRIVER", values);
    }

    public boolean hasValue(String v) {
        return getValues().contains(v);
    }

    /**
     * Adds the browser URI to the environment map.  Will override any values set in {@link SauceOnDemandBuildWrapper#setUp(hudson.model.AbstractBuild, hudson.Launcher, hudson.model.BuildListener)}
     *
     * @param value
     * @param map
     */
    public void addBuildVariable(String value, Map<String, String> map) {
        PluginImpl p = PluginImpl.get();
        String username;
        String accessKey;
        if (p.isReuseSauceAuth()) {
            SauceOnDemandAuthentication storedCredentials = null;
            storedCredentials = new SauceOnDemandAuthentication();
            username = storedCredentials.getUsername();
            accessKey = storedCredentials.getAccessKey();
        } else {
            username = p.getUsername();
            accessKey = Secret.toString(p.getApiKey());

        }
        Browser browserInstance = getBrowserForKey(value);
        if (browserInstance != null) {   // should never be null, but let's be defensive in case of downgrade.
            map.put(getName(), browserInstance.getUri(username, accessKey));
            map.put(SauceOnDemandBuildWrapper.SELENIUM_PLATFORM, browserInstance.getOs());
            map.put(SauceOnDemandBuildWrapper.SELENIUM_BROWSER, browserInstance.getBrowserName());
            map.put(SauceOnDemandBuildWrapper.SELENIUM_VERSION, browserInstance.getVersion());
            StringBuilder builder = new StringBuilder();
            builder.append("-D").append(getName()).append('=').append(browserInstance.getUri(username, accessKey)).
                    append("-D").append(SauceOnDemandBuildWrapper.SELENIUM_PLATFORM).append('=').append(browserInstance.getOs()).
                    append("-D").append(SauceOnDemandBuildWrapper.SELENIUM_BROWSER).append('=').append(browserInstance.getBrowserName()).
                    append("-D").append(SauceOnDemandBuildWrapper.SELENIUM_VERSION).append('=').append(browserInstance.getVersion());
            map.put("arguments", "-D" + getName() + "=" + browserInstance.getUri(username, accessKey));
        }
    }

    protected abstract Browser getBrowserForKey(String value);


}
