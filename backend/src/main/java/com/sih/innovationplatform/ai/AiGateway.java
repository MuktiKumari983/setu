package com.sih.innovationplatform.ai;

import com.sih.innovationplatform.config.AppProperties;

import java.util.List;

public interface AiGateway {

    default boolean isRemote() {
        return true;
    }

    default AiModels.AnalyzeResult analyze(String title, String description) {
        return analyze(title, description, null);
    }

    AiModels.AnalyzeResult analyze(String title, String description, String requiredOutcome);

    List<AiModels.SimilarProblem> duplicates(String title, String description,
                                             List<AiModels.ExistingProblem> existing);

    List<AiModels.Capability> decompose(String title, String description, List<String> skills);

    List<AiModels.ScoredMatch> match(MatchType type,
                                     AiModels.MatchingRequirement requirement,
                                     List<AiModels.CandidateProfile> candidates,
                                     AppProperties app);

    AiModels.Recommendation recommend(AiModels.MatchingRequirement requirement,
                                      List<AiModels.CandidateProfile> universities,
                                      List<AiModels.CandidateProfile> teams,
                                      List<AiModels.CandidateProfile> mentors,
                                      List<AiModels.CandidateProfile> industries,
                                      AppProperties app);

    AiModels.RecoveryRecommendation recover(String missingCapability,
                                            AiModels.MatchingRequirement requirement,
                                            List<AiModels.CandidateProfile> universities,
                                            List<AiModels.CandidateProfile> teams,
                                            List<AiModels.CandidateProfile> mentors,
                                            List<AiModels.CandidateProfile> industries,
                                            AppProperties app);
}