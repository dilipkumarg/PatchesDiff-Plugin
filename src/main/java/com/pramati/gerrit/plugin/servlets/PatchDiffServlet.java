package com.pramati.gerrit.plugin.servlets;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gerrit.extensions.annotations.PluginData;
import com.google.gerrit.reviewdb.client.Change;
import com.google.gerrit.reviewdb.client.Patch;
import com.google.gerrit.reviewdb.client.PatchSet;
import com.google.gerrit.reviewdb.server.ReviewDb;
import com.google.gerrit.server.patch.PatchList;
import com.google.gerrit.server.patch.PatchListCache;
import com.google.gerrit.server.patch.PatchListNotAvailableException;
import com.google.gson.Gson;
import com.google.gwtorm.server.OrmException;
import com.google.gwtorm.server.ResultSet;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.pramati.gerrit.plugin.cache.CacheData;
import com.pramati.gerrit.plugin.cache.CacheMetaData;
import com.pramati.gerrit.plugin.entities.ChangeIdDetails;
import com.pramati.gerrit.plugin.helpers.GetFileFromRepo;
import com.pramati.gerrit.plugin.helpers.MD5CheckSum;

@Singleton
public class PatchDiffServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final Provider<ReviewDb> requestDb;
	private final PatchListCache patchListCache;
	private ReviewDb reviewDb;
	static Logger logger = Logger.getLogger(PatchDiffServlet.class.getName());
	private final File pluginDir;

	@Inject
	PatchDiffServlet(final Provider<ReviewDb> requestDb,
			final PatchListCache patchListCache, @PluginData java.io.File myDir) {
		this.requestDb = requestDb;
		this.patchListCache = patchListCache;
		this.pluginDir = myDir;
	}

	@Override
	protected void doPost(final HttpServletRequest req,
			final HttpServletResponse rsp) throws IOException {
		// fileObj = new GetFileFromRepo(repoManager, requestDb, changeControl);
		// md5Obj = new MD5CheckSum();
		reviewDb = requestDb.get();
		PrintWriter out = rsp.getWriter();
		Change.Id changeId = getChangeIdFromRequest(req);

		if (changeId == null) {
			rsp.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid Change id");
			return;
		}

		try {
			out.print("<html>"
					+ "<head>"
					+ "<link rel=\"stylesheet\" type=\"text/css\" href=\"../static/bootstrap.css\">"
					+ "</head> <body>");
			StringBuilder str = new StringBuilder();
			str.append("<h1>Change Id:" + changeId.get() + "</h1><br>");
			ResultSet<PatchSet> patchsets = null;
			patchsets = reviewDb.patchSets().byChange(changeId);
			if (patchsets != null) {
				for (PatchSet patchset : patchsets) {
					str.append("<h3>Patch set:" + patchset.getPatchSetId()
							+ "</h3><br>");
					List<Patch> patches = doGetPatches(changeId, patchset);
					if (patches != null) {
						for (Patch patch : patches) {
							str.append("<p>File name:" + patch.getFileName());
							// getting file from git
							String request = makeRequestString(changeId.get(),
									patchset.getPatchSetId(),
									patch.getFileName());
							BufferedInputStream brStream = GetFileFromRepo
									.doGetFile(request.toString());
							// BufferedInputStream brStream = fileObj
							// .doGetFile(request.toString());
							if (brStream != null) {
								String Md5hash = new String();
								// Md5hash = md5Obj.ComputeMd5(brStream);
								Md5hash = MD5CheckSum.ComputeMd5(brStream);
								str.append(" :---: md5:");
								str.append(Md5hash);
							}
							str.append("</p> <br>");
						}
						str.append("<br>");
					}
				}
			}
			out.print(str.toString());
			out.print("</body> </html>");

		} catch (OrmException e) {
			// e.printStackTrace();
			getServletContext().log("cannot query database", e);
			rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (NoSuchAlgorithmException e) {
			rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			out.close();
		}
	}

	@Override
	protected void doGet(final HttpServletRequest req,
			final HttpServletResponse rsp) throws IOException {
		reviewDb = requestDb.get();
		PrintWriter out = rsp.getWriter();
		// rsp.setContentType("text/json");
		Change.Id changeId = getChangeIdFromRequest(req);

		if (changeId == null) {
			rsp.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid Change id");
			return;
		}
		try {
			ChangeIdDetails cidDetails;
			CacheMetaData cacheMeta = new CacheMetaData(pluginDir);
			CacheData cacheData = new CacheData(pluginDir);
			int noOfpatchSets_Cache = cacheMeta.getNoOfPatcheSets(changeId
					.get());
			int noOfpatchSets_Current = reviewDb.patchSets().byChange(changeId)
					.toList().size();

			if (noOfpatchSets_Current < 1) {
				rsp.sendError(HttpServletResponse.SC_NOT_FOUND,
						"Sorry given change Id do not have any patchsets");
				return;
			}
			// checking whether given id details exists in cache && here also
			// checking no of current patch sets in DB equal to patch sets in
			// cache
			if (noOfpatchSets_Cache != -1
					&& noOfpatchSets_Cache == noOfpatchSets_Current) {
				// directly fetching result from cache
				cidDetails = cacheData.getChangeIdDetails(changeId.get());

			} else {
				// getting details from repository
				cidDetails = getChangeIdDetails(changeId);
				if (cidDetails != null) {
					// putting the details in cache for later use
					cacheMeta.addOrUpdateChangeId(changeId.get(),
							noOfpatchSets_Current);
					cacheData.addUpdateChangeIdDetails(cidDetails);
				} else {
					rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
							"Database error occured");
					return;
				}

			}
			// converting to json string
			Gson gson = new Gson();
			String jsonString = gson.toJson(cidDetails, ChangeIdDetails.class);
			out.print(jsonString);
		} catch (OrmException e) {
			getServletContext().log("Database Error occured:", e);
			rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Database error occured");

		} catch (NoSuchAlgorithmException e) {
			getServletContext().log("MD5 Check sum computation failed:", e);
			rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			out.close();
		}

	}

	/**
	 * Builds the request string like (changeid,patchid,filename)
	 * 
	 * @param cid
	 * @param pid
	 * @param fname
	 * @return string
	 */
	private String makeRequestString(int cid, int pid, String fname) {
		StringBuilder request = new StringBuilder();
		request.append(cid);
		request.append(",");
		request.append(pid);
		request.append(",");
		request.append(fname);
		return request.toString();
	}

	/**
	 * cleans the request and parses change id from the request
	 * 
	 * @throws UnsupportedEncodingException
	 */
	private Change.Id getChangeIdFromRequest(HttpServletRequest req)
			throws UnsupportedEncodingException {
		String keyStr = req.getPathInfo();
		keyStr = URLDecoder.decode(keyStr, "UTF-8");
		if (!keyStr.startsWith("/")) {
			// rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		// deleting /(backslash) from the path
		keyStr = keyStr.substring(1);
		Change.Id cid;
		try {
			cid = Change.Id.parse(keyStr);
		} catch (NumberFormatException e) {
			// getServletContext().log("Invalid change Id", e);
			return null;
		}
		return cid;
	}

	/**
	 * retrieves list of patches for given patchset and change id.
	 * 
	 * @return List of patches
	 */
	private List<Patch> doGetPatches(Change.Id cid, PatchSet ps) {
		PatchList patchList;
		try {
			patchList = patchListCache.get(reviewDb.changes().get(cid), ps);
		} catch (PatchListNotAvailableException e) {
			getServletContext().log("Patch not Found in the Database", e);
			return null;
		} catch (OrmException e) {
			getServletContext().log("cannot query the database", e);
			return null;
		}

		List<Patch> patches = null;
		patches = patchList.toPatchList(ps.getId());
		return patches;
	}

	/**
	 * Returns the PatchSetDetails object. contains info regarding patches and
	 * its md5 check sum
	 * 
	 * @param cidDetails
	 * @param changeId
	 * @param patchset
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	private ChangeIdDetails.PatchSetDetails doGetPatchesDetails(
			ChangeIdDetails cidDetails, Change.Id changeId, PatchSet patchset)
			throws IOException, NoSuchAlgorithmException {
		ChangeIdDetails.PatchSetDetails psd = cidDetails.newPatchSet(patchset
				.getPatchSetId());
		List<Patch> patches = doGetPatches(changeId, patchset);
		if (patches != null) {
			for (Patch patch : patches) {
				// getting file from git
				String request = makeRequestString(changeId.get(),
						patchset.getPatchSetId(), patch.getFileName());
				BufferedInputStream brStream = GetFileFromRepo
						.doGetFile(request.toString());
				if (brStream != null) {
					String Md5hash = new String();
					Md5hash = MD5CheckSum.ComputeMd5(brStream);
					psd.addPatch(patch.getFileName(), Md5hash);
				}
			}
		}
		return psd;
	}

	/**
	 * Gets the ChangeIdDetails Object for given change Id
	 * 
	 * @param changeId
	 * @param rsp
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	private ChangeIdDetails getChangeIdDetails(Change.Id changeId)
			throws IOException, NoSuchAlgorithmException {
		ChangeIdDetails cidDetails = new ChangeIdDetails(changeId.get());
		try {
			ResultSet<PatchSet> patchsets = null;
			patchsets = reviewDb.patchSets().byChange(changeId);
			if (patchsets != null) {
				for (PatchSet patchset : patchsets) {
					ChangeIdDetails.PatchSetDetails psd = doGetPatchesDetails(
							cidDetails, changeId, patchset);

					cidDetails.addPatchSet(psd);
				}
			}

		} catch (OrmException e) {
			getServletContext().log("cannot query database", e);
			return null;
		}
		return cidDetails;
	}
}
