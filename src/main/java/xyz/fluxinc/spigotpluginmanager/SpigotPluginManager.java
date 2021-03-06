package xyz.fluxinc.spigotpluginmanager;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public final class SpigotPluginManager extends JavaPlugin {

    @Override
    public void onEnable() {
        File file = new File(getDataFolder(), "config.yml");
        YamlConfiguration config = new YamlConfiguration();
        if (!file.exists()) {
            saveResource("config.yml", false);
        }
        try { config.load(file); }
        catch (InvalidConfigurationException | IOException e) { e.printStackTrace(); getServer().getPluginManager().disablePlugin(this); }

        for (Plugin plugin : getServer().getPluginManager().getPlugins()) {
            try {
                sendRequest(config.getString("api-url"), config.getString("token"), generateVariables(plugin.getDescription()));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private List<NameValuePair> generateVariables(PluginDescriptionFile descriptionFile) {
        List<NameValuePair> variables = new ArrayList<>();

        variables.add(new BasicNameValuePair("name", descriptionFile.getName()));
        variables.add(new BasicNameValuePair("version", descriptionFile.getVersion()));

        if (descriptionFile.getDescription() != null) { variables.add(new BasicNameValuePair("description", descriptionFile.getDescription())); }
        if (!descriptionFile.getAuthors().isEmpty()) { variables.add(new BasicNameValuePair("authors", String.join(", ", descriptionFile.getAuthors()))); }
        if (!descriptionFile.getDepend().isEmpty()) { variables.add(new BasicNameValuePair("dependencies", String.join(", ", descriptionFile.getDepend()))); }
        if (!descriptionFile.getSoftDepend().isEmpty()) { variables.add(new BasicNameValuePair("soft_dependencies", String.join(",", descriptionFile.getSoftDepend()))); }
        if (descriptionFile.getWebsite() != null) { variables.add(new BasicNameValuePair("website", descriptionFile.getWebsite())); }

        return variables;
    }

    private void sendRequest(String apiUrl, String apiToken, List<NameValuePair> variables) throws UnsupportedEncodingException {
        HttpPost postReq = new HttpPost(apiUrl + "/plugins/add");
        variables.add(new BasicNameValuePair("api_token", apiToken));

        postReq.setEntity(new UrlEncodedFormEntity(variables));
        try {
            CloseableHttpClient client = HttpClients.createMinimal();

            client.execute(postReq);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
