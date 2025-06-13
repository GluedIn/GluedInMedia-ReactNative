export interface ChallengeResponse {
    status: string;
    statusCode: number;
    statusMessage: string;
    success: boolean;
    result: ChallengeResult;
  }
  
  export interface ChallengeResult {
    widgetEnabled: boolean;
    creatorEnabled: boolean;
    widgetTitle: LocalizedText;
    creatorTitle: LocalizedText;
    challengeInfo: ChallengeInfo;
  }
  
  export interface LocalizedText {
    en: string;
  }
  
  export interface ChallengeInfo {
    _id: string;
    hashtagId: string;
    title: string;
    description: string;
    image: string;
    bannerImage: string;
    backgroundImage: string;
    videoCount: number;
    status: string;
    createdAt: string;
    updatedAt: string;
    createdTimestamp: number;
    updatedTimestamp: number;
    isChallenge: number;
    startDate: string;
    endDate: string;
    projectId: string;
    businessId: string;
    leaderboardEnabled: boolean;
    leaderboardConfig: LeaderboardConfig;
    leaderboardCycleCount: number;
    widgetEnabled: boolean;
    creatorEnabled: boolean;
    thumbnailasBackgroundImage: boolean;
    _class: string;
    taggedVideos: any[];
    products: string[];
    lastRefreshTime: string;
    winnerAnnouncementTime: string;
    winnerList: WinnerList[];
  }
  
  export interface LeaderboardConfig {
    rankEvent: string;
    refreshTime: number;
    leaderboardEndDate: string;
    rewardToggle: boolean;
    winnerAnnouncement: string;
    gluedinReward: boolean;
    webhookEnabled: boolean;
    webhookUrl: string;
    winnerPoint: WinnerPoint;
    headerConfig: Record<string, string>;
  }
  
  export interface WinnerPoint {
    "1st": number;
    "2nd": number;
    "3rd": number;
  }
  
  export interface WinnerList {
    [key: string]: string[];
  }
  