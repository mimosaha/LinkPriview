package com.example.fblinkpreview.library;

import android.os.AsyncTask;
import android.util.Log;

import com.example.fblinkpreview.library.url.DefaultUrlExtractionStrategy;
import com.example.fblinkpreview.library.url.UrlExtractionStrategy;

import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextCrawler {

	public static final int ALL = -1;
	public static final int NONE = -2;

	private final String HTTP_PROTOCOL = "http://";
	private final String HTTPS_PROTOCOL = "https://";

	private LinkPreviewCallback callback;

	private AsyncTask getCodeTask;

    private UrlExtractionStrategy urlExtractionStrategy;

    public TextCrawler() {
    }

    public void makePreview(LinkPreviewCallback callback, String url) {
        ImagePickingStrategy imagePickingStrategy = new DefaultImagePickingStrategy();

        makePreview(callback, url, imagePickingStrategy);
    }

    public void makePreview(LinkPreviewCallback callback, String url, ImagePickingStrategy imagePickingStrategy) {
        this.callback = callback;
        cancel();
        getCodeTask = createPreviewGenerator(imagePickingStrategy).execute(url);
    }

    protected GetCode createPreviewGenerator(ImagePickingStrategy imagePickingStrategy) {
        return new GetCode(imagePickingStrategy, urlExtractionStrategy);
    }

	public void cancel(){
		if(getCodeTask != null){
			getCodeTask.cancel(true);
		}
	}

    /**
     * Get html code
     */
     class GetCode extends AsyncTask<String, Void, Void> {

        private SourceContent sourceContent = new SourceContent();
        private final ImagePickingStrategy imagePickingStrategy;
        private final UrlExtractionStrategy urlExtractionStrategy;

        GetCode(ImagePickingStrategy imagePickingStrategy, UrlExtractionStrategy urlExtractionStrategy) {
            this.imagePickingStrategy = imagePickingStrategy;
            if (urlExtractionStrategy == null) {
                urlExtractionStrategy = new DefaultUrlExtractionStrategy();
            }
            this.urlExtractionStrategy = urlExtractionStrategy;
        }

        @Override
        protected void onPreExecute() {
            if (callback != null) {
                callback.onPre();
            }
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            if (callback != null) {
                callback.onPos(sourceContent, isNull());
            }
            super.onPostExecute(result);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected Void doInBackground(String... params) {
            final List<String> urlStrings = urlExtractionStrategy.extractUrls(params[0]);
            String url;
            if (urlStrings != null && !urlStrings.isEmpty()) {
                url = unshortenUrl1(urlStrings.get(0));
            } else {
                url = "";
            }

            sourceContent.setFinalUrl(url);
            boolean wasPreviewGenerationSuccessful = false;
            if (!url.equals("")) {
                if (isImage(url) && !url.contains("dropbox")) {
                    setSourceContentForImage();
                    wasPreviewGenerationSuccessful = true;
                } else {
                    try {
                        Document doc = getDocument();

                        sourceContent.setHtmlCode(extendedTrim(doc.toString()));

                        HashMap<String, String> metaTags = getMetaTags(sourceContent
                                .getHtmlCode());

                        sourceContent.setMetaTags(metaTags);

                        sourceContent.setTitle(doc.title());
                        sourceContent.setDescription(metaTags
                                .get("description"));

                        if (sourceContent.getTitle().equals("")) {
                            String matchTitle = Regex.pregMatch(
                                    sourceContent.getHtmlCode(),
                                    Regex.TITLE_PATTERN, 2);

                            if (!matchTitle.equals(""))
                                sourceContent.setTitle(htmlDecode(matchTitle));
                        }

                        if (sourceContent.getDescription().equals(""))
                            sourceContent
                                    .setDescription(crawlCode(sourceContent
                                            .getHtmlCode()));

                        sourceContent.setDescription(sourceContent
                                .getDescription().replaceAll(
                                        Regex.SCRIPT_PATTERN, ""));

                        if (imagePickingStrategy.getImageQuantity() != NONE) {
                            List<String> images;
                            images = imagePickingStrategy.getImages(getCodeTask, doc, metaTags);
                            sourceContent.setImages(images);
                        }

                        wasPreviewGenerationSuccessful = true;
                    } catch (Throwable t) {
                        if (t instanceof UnsupportedMimeTypeException) {
                            final String mimeType = ((UnsupportedMimeTypeException) t).getMimeType();
                            if (mimeType != null && mimeType.startsWith("image")) {
                                setSourceContentForImage();
                                wasPreviewGenerationSuccessful = true;
                            }
                        }
                    }
                }
                sourceContent.setSuccess(wasPreviewGenerationSuccessful);
            }

            String[] finalLinkSet = sourceContent.getFinalUrl().split("&");
            sourceContent.setUrl(finalLinkSet[0]);

            sourceContent.setCannonicalUrl(cannonicalPage(sourceContent
                    .getFinalUrl()));
            sourceContent.setDescription(stripTags(sourceContent
                    .getDescription()));

            return null;
        }

        /**
         * Configures the sourceContent for an Image.
         */
        private void setSourceContentForImage() {
            sourceContent.getImages().add(sourceContent.getFinalUrl());

            sourceContent.setTitle("");
            sourceContent.setDescription("");
        }

        protected Document getDocument() throws IOException {
            return Jsoup.connect(sourceContent.getFinalUrl()).userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36").get();
        }

		/** Verifies if the content could not be retrieved */
		public boolean isNull() {
			return !sourceContent.isSuccess() &&
				extendedTrim(sourceContent.getHtmlCode()).equals("") &&
				!isImage(sourceContent.getFinalUrl());
		}

	}

	/** Gets content from a html tag */
	private String getTagContent(String tag, String content) {

		String pattern = "<" + tag + "(.*?)>(.*?)</" + tag + ">";
		String result = "", currentMatch = "";

		List<String> matches = Regex.pregMatchAll(content, pattern, 2);

		int matchesSize = matches.size();
		for (int i = 0; i < matchesSize; i++) {
			if(getCodeTask.isCancelled()){
				break;
			}
			currentMatch = stripTags(matches.get(i));
			if (currentMatch.length() >= 120) {
				result = extendedTrim(currentMatch);
				break;
			}
		}

		if (result.equals("")) {
			String matchFinal = Regex.pregMatch(content, pattern, 2);
			result = extendedTrim(matchFinal);
		}

		result = result.replaceAll("&nbsp;", "");

		return htmlDecode(result);
	}

	/** Transforms from html to normal string */
	private String htmlDecode(String content) {
		return Jsoup.parse(content).text();
	}

    /**
     * Crawls the code looking for relevant information
     */
    private String crawlCode(String content) {
        String resultSpan = getTagContent("span", content);
        String resultParagraph = getTagContent("p", content);
        String resultDiv = getTagContent("div", content);

        String result;

        if (resultParagraph.length() > resultSpan.length()
                && resultParagraph.length() >= resultDiv.length())
            result = resultParagraph;
        else if (resultParagraph.length() > resultSpan.length()
                && resultParagraph.length() < resultDiv.length())
            result = resultDiv;
        else
            result = resultParagraph;

        return htmlDecode(result);
    }

	/** Returns the cannoncial url */
	private String cannonicalPage(String url) {

		String cannonical = "";
		if (url.startsWith(HTTP_PROTOCOL)) {
			url = url.substring(HTTP_PROTOCOL.length());
		} else if (url.startsWith(HTTPS_PROTOCOL)) {
			url = url.substring(HTTPS_PROTOCOL.length());
		}

		int urlLength = url.length();
		for (int i = 0; i < urlLength; i++) {
			if(getCodeTask.isCancelled()){
				break;
			}
			if (url.charAt(i) != '/')
				cannonical += url.charAt(i);
			else
				break;
		}

		return cannonical;

	}

	/** Strips the tags from an element */
	private String stripTags(String content) {
		return Jsoup.parse(content).text();
	}

	/** Verifies if the url is an image */
	private boolean isImage(String url) {
		return url.matches(Regex.IMAGE_PATTERN);
	}

	/**
	 * Returns meta tags from html code
	 */
	private HashMap<String, String> getMetaTags(String content) {

		HashMap<String, String> metaTags = new HashMap<String, String>();
		metaTags.put("url", "");
		metaTags.put("title", "");
		metaTags.put("description", "");
		metaTags.put("image", "");

		List<String> matches = Regex.pregMatchAll(content,
				Regex.METATAG_PATTERN, 1);

		for (String match : matches) {
			if(getCodeTask.isCancelled()){
				break;
			}
			final String lowerCase = match.toLowerCase();
			if (lowerCase.contains("property=\"og:url\"")
					|| lowerCase.contains("property='og:url'")
					|| lowerCase.contains("name=\"url\"")
					|| lowerCase.contains("name='url'"))
				updateMetaTag(metaTags, "url", separeMetaTagsContent(match));
			else if (lowerCase.contains("property=\"og:title\"")
					|| lowerCase.contains("property='og:title'")
					|| lowerCase.contains("name=\"title\"")
					|| lowerCase.contains("name='title'"))
				updateMetaTag(metaTags, "title", separeMetaTagsContent(match));
			else if (lowerCase
					.contains("property=\"og:description\"")
					|| lowerCase
					.contains("property='og:description'")
					|| lowerCase.contains("name=\"description\"")
					|| lowerCase.contains("name='description'"))
				updateMetaTag(metaTags, "description", separeMetaTagsContent(match));
			else if (lowerCase.contains("property=\"og:image\"")
					|| lowerCase.contains("property='og:image'")
					|| lowerCase.contains("name=\"image\"")
					|| lowerCase.contains("name='image'"))
				updateMetaTag(metaTags, "image", separeMetaTagsContent(match));
		}

		return metaTags;
	}

	private void updateMetaTag(HashMap<String, String> metaTags, String url, String value) {
		if (value != null && (value.length() > 0)) {
			metaTags.put(url, value);
		}
	}

	/** Gets content from metatag */
	private String separeMetaTagsContent(String content) {
		String result = Regex.pregMatch(content, Regex.METATAG_CONTENT_PATTERN,
				1);
		return htmlDecode(result);
	}

    /**
     * Unshortens a short url
     */
    private String unshortenUrl1(final String originURL) {
        String finalUrl = "";
        if (!originURL.startsWith(HTTP_PROTOCOL)
                && !originURL.startsWith(HTTPS_PROTOCOL))
            return "";

        try {
            URL url = new URL(originURL);
            HttpURLConnection httpURLConnection =
                    (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.setReadTimeout(10000);

            httpURLConnection.connect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        URLConnection urlConn = connectURL(originURL);
        Map<String,List<String>> map = urlConn.getHeaderFields();

        if (map != null && map.size() > 0) {

            List<String> nullValue = map.get(null);

            if (nullValue != null && nullValue.size() > 0) {
                if (!nullValue.get(0).equals("HTTP/1.1 200 OK")) {
                    if (map.containsKey("Location")) {
                        List<String> locationValue = map.get("Location");
                        if (locationValue != null && locationValue.size() > 0) {
                            finalUrl = locationValue.get(0);
                        } else {
                            finalUrl = originURL;
                        }
                    } else {
                        finalUrl = originURL;
                    }
                } else {
                    finalUrl = originURL;
                }
            }
        }
        return finalUrl;
    }

    private String unshortenUrl(final String originURL) {
        try {
            if (!originURL.startsWith(HTTP_PROTOCOL)
                    && !originURL.startsWith(HTTPS_PROTOCOL))
                return "";
            URLConnection urlConn = connectURL(originURL);
            urlConn.getHeaderFields();
            final URL finalUrl = urlConn.getURL();

            urlConn = connectURL(finalUrl);
            urlConn.getHeaderFields();
            final URL shortURL = urlConn.getURL();

            String finalResult = shortURL.toString();

            while (!shortURL.sameFile(finalUrl)) {
                boolean isEndlesslyRedirecting = false;
                if (shortURL.getHost().equals(finalUrl.getHost())) {
                    if (shortURL.getPath().equals(finalUrl.getPath())) {
                        isEndlesslyRedirecting = true;
                    }
                }
                if (isEndlesslyRedirecting) {
                    break;
                } else {
                    finalResult = unshortenUrl(shortURL.toString());
                }
            }
            return finalResult;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Takes a valid url string and returns a URLConnection object for the url.
     */
    private URLConnection connectURL(String strURL) {
        URLConnection conn = null;
        try {
            URL inputURL = new URL(strURL);
            conn = connectURL(inputURL);
        } catch (MalformedURLException e) {
            System.out.println("Please input a valid URL");
        }
        return conn;
    }

    /**
     * Takes a valid url and returns a URLConnection object for the url.
     */
    private URLConnection connectURL(URL inputURL) {
        URLConnection conn = null;
        try {
            conn = inputURL.openConnection();
        } catch (IOException ioe) {
            System.out.println("Can not connect to the URL");
        }
        return conn;
    }

	/** Removes extra spaces and trim the string */
	public static String extendedTrim(String content) {
		return content.replaceAll("\\s+", " ").replace("\n", " ")
				.replace("\r", " ").trim();
	}

}
