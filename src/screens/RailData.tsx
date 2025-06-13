// Define the Product model
export interface Product {
    discountPrice: number;
    _id: string;
    shoppableLink: string;
    imageUrl: string;
    callToAction: string;
    discount_endDate: number;
    productName: string;
    discount_startDate: number;
    mrp: number;
  }
  
  // Define the Tagged User model
  export interface TaggedUser {
    fullName: string;
    email: string;
    userId: string;
    profileImageUrl: string;
    userName: string;
  }
  
  // Define the Content URL model
  export interface ContentUrl {
    urls: string[];
    width: number;
    type: string;
    height: number;
    factor: number;
    duration: number;
  }
  
  // Define the User model
  export interface User {
    fullName: string;
    userName: string;
    followersCount: number;
    userId: string;
    profileImageUrl: string;
  }
  
  // Define the Hashtag model
  export interface Hashtag {
    hashtagId: string;
    isChallenge: number;
    title: string;
  }
  
  // Define the Video model
  export interface Video {
    products: Product[];
    shoppable: boolean;
    videoId: string;
    commentEnabled: boolean;
    userId: string;
    downloadUrl: string;
    description: string;
    thumbnailUrl: string;
    topicId: string;
    likeCount: number;
    taggedUsers: TaggedUser[];
    downloadEnabled: boolean;
    contentUrls: ContentUrl[];
    originalVideoId: string;
    labels: string[];
    user: User;
    repost: boolean;
    contentType: string;
    hashtags: Hashtag[];
    repostCount: number;
    _id: string;
    commentCount: number;
    shareEnabled: boolean;
    allowedRegions: string[];
    status: string;
    likeEnabled: boolean;
    viewsCount: number;
    categoryId: string;
    title: string;
  }
  
  // Define the main API response model
  export interface RailDataItem {
    thumbnail: string;
    firstName?: string;
    lastName?: string;
    displayName: string;
    pristine_image?: string;
    id: string;
    bannerUrl?: string;
    followers: number;
    likeCount: number;
    ordering: number;
    video: Video;
    assetId: string;
    description: string;
    contentType: string;
  }
  
  // Define the API response as an array of `ApiResponseItem`
  export type RailData = RailDataItem[];