package org.sobotics.guttenberg.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sobotics.guttenberg.utils.ApiUtils;
import org.sobotics.guttenberg.utils.FilePathUtils;
import org.sobotics.guttenberg.utils.FileUtils;
import org.sobotics.guttenberg.utils.JsonUtils;

import com.google.gson.JsonObject;

/**
 * Created by bhargav.h on 29-Sep-16.
 */
public class ApiService {

	public static ApiService defaultService = new ApiService("stackoverflow");
	
    private String apiKey;
    private String autoflagKey;
    private String autoflagToken;
    private String userId;
    private String site;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiService.class);

    private static int quota=0;

    public ApiService(String site){
        Properties prop = new Properties();

        try{
            prop = FileUtils.getPropertiesFromFile(FilePathUtils.loginPropertiesFile);
        }
        catch (IOException e){
            e.printStackTrace();
        }

        this.site = site;
        this.apiKey = prop.getProperty("apikey");
        this.autoflagKey = prop.getProperty("autoflagkey");
        this.autoflagToken = prop.getProperty("autoflagtoken");
        this.userId = prop.getProperty("userid");


    }
    
    public JsonObject getRelatedQuestionsByIds(String questionIds) throws IOException {
    	JsonObject questionJson = ApiUtils.getRelatedQuestionsByIds(questionIds, site, apiKey);
    	JsonUtils.handleBackoff(LOGGER, questionJson);
        quota = questionJson.get("quota_remaining").getAsInt();
        return questionJson;
    }
    
    public JsonObject getLinkedQuestionsByIds(String questionIds) throws IOException {
    	JsonObject questionJson = ApiUtils.getLinkedQuestionsByIds(questionIds, site, apiKey);
    	JsonUtils.handleBackoff(LOGGER, questionJson);
        quota = questionJson.get("quota_remaining").getAsInt();
        return questionJson;
    }
    
    public JsonObject getAnswersToQuestionsByIdString(String questionIds) throws IOException {
    	return getAnswersToQuestionsByIdString(questionIds, 1);
    }
    
    public JsonObject getAnswersToQuestionsByIdString(String questionIds, Integer page) throws IOException {
    	JsonObject answersJson = ApiUtils.getAnswersToQuestionsByIdString(questionIds, page, site, apiKey);
    	JsonUtils.handleBackoff(LOGGER, answersJson);
        quota = answersJson.get("quota_remaining").getAsInt();
        return answersJson;
    }

    public JsonObject getQuestionDetailsByIds(List<Integer> questionIdList) throws IOException {
        JsonObject questionJson = ApiUtils.getQuestionDetailsByIds(questionIdList,site,apiKey);
        JsonUtils.handleBackoff(LOGGER, questionJson);
        quota = questionJson.get("quota_remaining").getAsInt();
        return questionJson;
    }

    public JsonObject getQuestionDetailsById(Integer questionId) throws IOException{
        JsonObject questionJson = ApiUtils.getQuestionDetailsById(questionId,site,apiKey);
        JsonUtils.handleBackoff(LOGGER, questionJson);
        quota = questionJson.get("quota_remaining").getAsInt();
        return questionJson;
    }

    public JsonObject getAnswerDetailsById(Integer answerId) throws IOException{
        JsonObject answerJson = ApiUtils.getAnswerDetailsById(answerId,site,apiKey);
        JsonUtils.handleBackoff(LOGGER, answerJson);
        quota = answerJson.get("quota_remaining").getAsInt();
        return answerJson;
    }

    public JsonObject getAnswerDetailsByIds(List<Integer> answerIdList) throws IOException{
        JsonObject answerJson = ApiUtils.getAnswerDetailsByIds(answerIdList,site,apiKey);
        JsonUtils.handleBackoff(LOGGER, answerJson);
        quota = answerJson.get("quota_remaining").getAsInt();
        return answerJson;
    }

    public JsonObject getFirstPageOfAnswers(Instant fromTimestamp) throws IOException{
        JsonObject answersJson = ApiUtils.getFirstPageOfAnswers(fromTimestamp,site,apiKey);
        JsonUtils.handleBackoff(LOGGER, answersJson);
        quota = answersJson.get("quota_remaining").getAsInt();
        return answersJson;
    }
    
    public JsonObject getAnswerFlagOptions(Integer answerId) throws IOException{
        JsonObject flagOptionsJson = ApiUtils.getAnswerFlagOptions(answerId,site,autoflagKey,autoflagToken);
        JsonUtils.handleBackoff(LOGGER, flagOptionsJson);
        quota = flagOptionsJson.get("quota_remaining").getAsInt();
        return flagOptionsJson;
    }

    public JsonObject flagAnswer(Integer answerId, Integer flagType) throws IOException{
        JsonObject flaggedPost = ApiUtils.FlagAnswer(answerId,flagType,site,autoflagKey,autoflagToken);
        JsonUtils.handleBackoff(LOGGER, flaggedPost);
        quota = flaggedPost.get("quota_remaining").getAsInt();
        return flaggedPost;
    }

    public int getQuota(){
        return quota;
    }
}
