package hudson.plugins.sauce_ondemand;

import com.saucelabs.ci.Browser;
import com.saucelabs.ci.BrowserFactory;
import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.ParameterValue;
import hudson.util.Secret;
import net.sf.json.JSONArray;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 *
 * @author Ross Rowe
 */
public class SauceParameterValue extends ParameterValue {

    /** Handles the retrieval of browsers from Sauce Labs. */
    private static final BrowserFactory BROWSER_FACTORY = BrowserFactory.getInstance(new JenkinsSauceREST(null, null));

    private final JSONArray selectedBrowsers;

    @DataBoundConstructor
    public SauceParameterValue(String name, JSONArray selectedBrowsers) {
        super(name);
        this.selectedBrowsers = selectedBrowsers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {

        if (selectedBrowsers != null && !selectedBrowsers.isEmpty()) {
            if (selectedBrowsers.size() == 1) {
                Browser browserInstance = BROWSER_FACTORY.webDriverBrowserForKey(selectedBrowsers.getString(0));
                SauceEnvironmentUtil.outputEnvironmentVariablesForBrowser(env, browserInstance, getUserName(build), getApiKey(build), true, false, null);
            } else {
                JSONArray browsersJSON = new JSONArray();
                for (int i = 0; i < selectedBrowsers.size(); i++) {
                    String browser = selectedBrowsers.getString(i);
                    {
                        Browser browserInstance = BrowserFactory.getInstance().webDriverBrowserForKey(browser);
                        SauceEnvironmentUtil.browserAsJSON(browsersJSON, browserInstance, getUserName(build), getApiKey(build));
                        //output SELENIUM_DRIVER for the first browser so that the Selenium Client Factory picks up a valid uri pattern
                        SauceEnvironmentUtil.outputEnvironmentVariable(env, SauceOnDemandBuildWrapper.SELENIUM_DRIVER, browserInstance.getUri(getUserName(build), getApiKey(build)), true, false, null);
                    }
                    SauceEnvironmentUtil.outputEnvironmentVariable(env, SauceOnDemandBuildWrapper.SAUCE_ONDEMAND_BROWSERS, browsersJSON.toString(), true, false, null);

                }
            }
        }

    }

    private String getApiKey(AbstractBuild<?, ?> build) {

        SauceOnDemandBuildWrapper buildWrapper = getBuildWrapper(build);
        if (buildWrapper == null) {
            return Secret.toString(PluginImpl.get().getApiKey());
        }
        return buildWrapper.getApiKey();
    }

    private String getUserName(AbstractBuild<?, ?> build) {
        SauceOnDemandBuildWrapper buildWrapper = getBuildWrapper(build);
        if (buildWrapper == null) {
            return PluginImpl.get().getUsername();
        }
        return getBuildWrapper(build).getUserName();
    }

    private SauceOnDemandBuildWrapper getBuildWrapper(AbstractBuild<?, ?> build) {
        return SauceEnvironmentUtil.getBuildWrapper(build.getProject());
    }


}
