package nnu.wyz.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/28 10:19
 */
@Service("mongoClientDetails")
public class MongoClientDetailsServiceIml implements ClientDetailsService {
    private final String COLLECTIONNAME = "oauth_client_details";
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    PasswordEncoder bcyptPasswordEncoder;

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        return mongoTemplate.findOne(new Query(Criteria.where("clientId").is(clientId)), BaseClientDetails.class, COLLECTIONNAME);
    }

    public void addClientDetails(ClientDetails clientDetails) {
        mongoTemplate.insert(clientDetails, COLLECTIONNAME);
    }

    public void updateClientDetails(ClientDetails clientDetails) {
        Update update = new Update();
        update.set("resourceIds", clientDetails.getResourceIds());
        update.set("clientSecret", clientDetails.getClientSecret());
        update.set("authorizedGrantTypes", clientDetails.getAuthorizedGrantTypes());
        update.set("registeredRedirectUris", clientDetails.getRegisteredRedirectUri());
        update.set("authorities", clientDetails.getAuthorities());
        update.set("accessTokenValiditySeconds", clientDetails.getAccessTokenValiditySeconds());
        update.set("refreshTokenValiditySeconds", clientDetails.getRefreshTokenValiditySeconds());
        update.set("additionalInformation", clientDetails.getAdditionalInformation());
        update.set("scope", clientDetails.getScope());
        mongoTemplate.updateFirst(new Query(Criteria.where("clientId").is(clientDetails.getClientId())), update, COLLECTIONNAME);
    }

    public void updateClientSecret(String clientId, String secret) {
        Update update = new Update();
        update.set("clientSecret", secret);
        mongoTemplate.updateFirst(new Query(Criteria.where("clientId").is(clientId)), update, COLLECTIONNAME);
    }

    public void removeClientDetails(String clientId) {
        mongoTemplate.remove(new Query(Criteria.where("clientId").is(clientId)), COLLECTIONNAME);
    }

    public List<ClientDetails> listClientDetails() {
        return mongoTemplate.findAll(ClientDetails.class, COLLECTIONNAME);
    }
}
