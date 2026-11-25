package com.adriperez.squadmanager.service;

import com.adriperez.squadmanager.exception.BusinessRuleException;
import com.adriperez.squadmanager.exception.ResourceNotFoundException;
import com.adriperez.squadmanager.model.AppUser;
import com.adriperez.squadmanager.model.Match;
import com.adriperez.squadmanager.model.MatchAppearance;
import com.adriperez.squadmanager.model.enums.CallUpStatus;
import com.adriperez.squadmanager.model.enums.Position;
import com.adriperez.squadmanager.repository.MatchRepository;
import com.adriperez.squadmanager.security.CurrentUserService;
import org.openpdf.text.*;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class CallUpPdfService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy · HH:mm 'h'", new Locale("es", "ES"));

    private final MatchRepository matchRepository;
    private final CurrentUserService currentUserService;

    public CallUpPdfService(MatchRepository matchRepository, CurrentUserService currentUserService) {
        this.matchRepository = matchRepository;
        this.currentUserService = currentUserService;
    }

    public byte[] generateCallUpPdf(Long matchId, boolean includeRoles) {
        AppUser currentUser = currentUserService.getCurrentUser();

        Match match = matchRepository.findByIdAndUserIdWithAppearances(matchId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + matchId));

        List<MatchAppearance> calledUpPlayers = match.getMatchAppearances().stream()
                .filter(a -> a.getCallUpStatus() != CallUpStatus.NOT_CALLED)
                .sorted(Comparator
                        .comparingInt((MatchAppearance a) -> positionOrder(a.getPlayer().getPosition()))
                        .thenComparing(a -> a.getPlayer().getShirtNumber(), Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        if (calledUpPlayers.isEmpty()) {
            throw new BusinessRuleException("No hay jugadores convocados en este partido para generar el PDF");
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            Color darkBg = new Color(15, 23, 42);
            Color emerald = new Color(16, 185, 129);
            Color lightGray = new Color(241, 245, 249);
            Color borderGray = new Color(203, 213, 225);
            Color textMuted = new Color(100, 116, 139);

            // 1. Cabecera Corporativa del Club
            PdfPTable banner = new PdfPTable(1);
            banner.setWidthPercentage(100);
            PdfPCell bannerCell = new PdfPCell();
            bannerCell.setBackgroundColor(darkBg);
            bannerCell.setPadding(18f);
            bannerCell.setBorder(Rectangle.NO_BORDER);

            Font tagFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, emerald);
            Paragraph tag = new Paragraph("CONVOCATORIA OFICIAL · " + match.getCompetition().toUpperCase(), tagFont);
            tag.setSpacingAfter(4f);
            bannerCell.addElement(tag);

            Font clubFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.WHITE);
            Paragraph clubTitle = new Paragraph(currentUser.getClubName().toUpperCase(), clubFont);
            clubTitle.setSpacingAfter(6f);
            bannerCell.addElement(clubTitle);

            String condition = Boolean.TRUE.equals(match.getHomeMatch()) ? "LOCAL" : "VISITANTE";
            Font rivalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, new Color(226, 232, 240));
            Paragraph rivalLine = new Paragraph("vs " + match.getOpponent() + " (" + condition + ")", rivalFont);
            bannerCell.addElement(rivalLine);

            banner.addCell(bannerCell);
            banner.setSpacingAfter(14f);
            document.add(banner);

            // 2. Caja de Datos del Encuentro
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{65f, 35f});
            infoTable.setSpacingAfter(16f);

            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, textMuted);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, darkBg);

            String formattedDate = match.getMatchDate() != null
                    ? capitalize(match.getMatchDate().format(DATE_FORMATTER))
                    : "Fecha por confirmar";

            infoTable.addCell(buildInfoBox("FECHA Y HORA DEL ENCUENTRO", formattedDate, labelFont, valueFont, lightGray, borderGray));
            infoTable.addCell(buildInfoBox("TOTAL CONVOCADOS", calledUpPlayers.size() + " Jugadores", labelFont, valueFont, lightGray, borderGray));
            document.add(infoTable);

            // 3. Tabla de Jugadores Convocados (3 o 4 columnas según includeRoles)
            PdfPTable squadTable = includeRoles ? new PdfPTable(4) : new PdfPTable(3);
            squadTable.setWidthPercentage(100);
            squadTable.setWidths(includeRoles ? new float[]{12f, 44f, 24f, 20f} : new float[]{15f, 53f, 32f});

            Font thFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            String[] headers = includeRoles
                    ? new String[]{"DORSAL", "JUGADOR", "DEMARCACIÓN", "ROL"}
                    : new String[]{"DORSAL", "JUGADOR", "DEMARCACIÓN"};

            for (int i = 0; i < headers.length; i++) {
                PdfPCell th = new PdfPCell(new Phrase(headers[i], thFont));
                th.setBackgroundColor(darkBg);
                th.setPadding(8f);
                th.setBorderColor(darkBg);
                th.setHorizontalAlignment(i == 0 || i == 3 ? Element.ALIGN_CENTER : Element.ALIGN_LEFT);
                squadTable.addCell(th);
            }

            Font dorsalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(5, 150, 105));
            Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, darkBg);
            Font posFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(51, 65, 85));
            Font roleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, darkBg);

            boolean alternate = false;
            for (MatchAppearance app : calledUpPlayers) {
                Color rowBg = alternate ? lightGray : Color.WHITE;
                alternate = !alternate;

                PdfPCell cDorsal = new PdfPCell(new Phrase("#" + app.getPlayer().getShirtNumber(), dorsalFont));
                cDorsal.setHorizontalAlignment(Element.ALIGN_CENTER);
                styleDataCell(cDorsal, rowBg, borderGray);
                squadTable.addCell(cDorsal);

                String fullName = app.getPlayer().getFirstName() + " " + app.getPlayer().getLastName();
                PdfPCell cName = new PdfPCell(new Phrase(fullName, nameFont));
                styleDataCell(cName, rowBg, borderGray);
                squadTable.addCell(cName);

                PdfPCell cPos = new PdfPCell(new Phrase(translatePosition(app.getPlayer().getPosition()), posFont));
                styleDataCell(cPos, rowBg, borderGray);
                squadTable.addCell(cPos);

                if (includeRoles) {
                    String roleLabel = app.getCallUpStatus() == CallUpStatus.STARTER ? "TITULAR" : "SUPLENTE";
                    PdfPCell cRole = new PdfPCell(new Phrase(roleLabel, roleFont));
                    cRole.setHorizontalAlignment(Element.ALIGN_CENTER);
                    styleDataCell(cRole, rowBg, borderGray);
                    squadTable.addCell(cRole);
                }
            }

            document.add(squadTable);

            // 4. Pie de página
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, textMuted);
            Paragraph footer = new Paragraph("Documento generado automáticamente por SquadManager Pro", footerFont);
            footer.setAlignment(Element.ALIGN_RIGHT);
            footer.setSpacingBefore(14f);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new BusinessRuleException("No se pudo generar el documento PDF de la convocatoria");
        }
    }

    private PdfPCell buildInfoBox(String label, String value, Font labelFont, Font valueFont, Color bg, Color border) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bg);
        cell.setBorderColor(border);
        cell.setPadding(10f);
        Paragraph pLabel = new Paragraph(label, labelFont);
        pLabel.setSpacingAfter(2f);
        cell.addElement(pLabel);
        cell.addElement(new Paragraph(value, valueFont));
        return cell;
    }

    private void styleDataCell(PdfPCell cell, Color bg, Color border) {
        cell.setBackgroundColor(bg);
        cell.setBorderColor(border);
        cell.setPadding(7f);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    }

    private int positionOrder(Position pos) {
        if (pos == null) return 99;
        return switch (pos) {
            case GOALKEEPER -> 1;
            case DEFENDER -> 2;
            case MIDFIELDER -> 3;
            case FORWARD -> 4;
        };
    }

    private String translatePosition(Position pos) {
        if (pos == null) return "-";
        return switch (pos) {
            case GOALKEEPER -> "Portero";
            case DEFENDER -> "Defensa";
            case MIDFIELDER -> "Centrocampista";
            case FORWARD -> "Delantero";
        };
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}