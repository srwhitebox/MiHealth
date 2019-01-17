package com.mihealth.webservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBElement;

import org.apache.commons.codec.CharEncoding;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.BodyElementType;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.docx4j.Docx4J;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTAltChunk;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Document;
import org.docx4j.wml.P;
import org.docx4j.wml.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mihealth.db.model.LocaleModel;
import com.mihealth.db.service.StudentService;
import com.ximpl.lib.doc.DocxDocument;
import com.ximpl.lib.doc.DocxTemplate;
import com.ximpl.lib.doc.WordTemplate;

@Controller
public class PoiApi {
	private static final Logger logger = Logger.getLogger(PoiApi.class);
	@Autowired
	StudentService studentService;

	@RequestMapping(value = {"poi/docx"}, method = RequestMethod.GET, produces = "text/plain;charset=UTF-8", headers = "Accept=application/json")
	@ResponseBody public void add(HttpServletRequest request, HttpServletResponse response) {
		final String filePath = studentService.getTemplatePath("Bảng khám sức khỏe định kỳ.docx");
		DocxTemplate template = new DocxTemplate(filePath);
		template.setModel("student.name", "David Kim");
		WordprocessingMLPackage wordPkg = template.patch();
		DocxDocument docx = new DocxDocument();
		docx.insert(wordPkg);
		docx.insert(wordPkg);
		docx.insert(wordPkg);

		docx.write("d:\\Downloads\\result.docx");
	}
		
}

//t.setValue(t.getValue().replace(toFind, replacer));
