import React, { useEffect, useState } from 'react';
import { supabase } from '../../supabaseClient'; // ✅ Import Supabase client
import TopNav from "../NavBars/TopNav";

const Reports = () => {
    const [reports, setReports] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchReports();
    }, []);

    // Fetch reports from Supabase
    const fetchReports = async () => {
        try {
            const { data, error } = await supabase
            .from('reports')
            .select(`
                report_id,
                link_reported,
                report_description,
                approved,
                users:userid(userusername)
            `);
            if (error) {
                console.error('Error fetching reports:', error.message);
            } else {
                console.log('Fetched reports data:', data); // ✅ Log fetched data
                setReports(data);
            }
        } catch (error) {
            console.error('Error fetching reports:', error.message);
        } finally {
            setLoading(false);
        }
    };

    // Approve a report
    const handleApprove = async (report_id, link) => {
        if (window.confirm("Are you sure you want to approve this report?")) {
            try {
                const { error } = await supabase
                    .from('reports')
                    .update({ approved: true })
                    .match({ report_id, link_reported: link }); // Match both report_id and link_reported
    
                if (error) {
                    console.error('Error approving report:', error.message);
                } else {
                    alert("Report approved successfully!");
                    setReports(prevReports =>
                        prevReports.map(report =>
                            report.report_id === report_id ? { ...report, approved: true } : report
                        )
                    );
                }
            } catch (error) {
                console.error('Error approving report:', error.message);
            }
        }
    };

    // Delete a report
    const handleDelete = async (report_id) => {
        if (window.confirm("Are you sure you want to delete this report?")) {
            try {
                const { error } = await supabase
                    .from('reports')
                    .delete()
                    .match({ report_id });

                if (error) {
                    console.error('Error deleting report:', error.message);
                } else {
                    setReports(prevReports => prevReports.filter(report => report.report_id !== report_id));
                }
            } catch (error) {
                console.error('Error deleting report:', error.message);
            }
        }
    };

    // Print reports (approved or not)
    const handlePrint = (type) => {
        const filteredReports = reports.filter(report =>
            type === 'approved' ? report.approved : !report.approved
        );

        const printWindow = window.open('', '_blank');
        printWindow.document.write(`
            <html>
                <head>
                    <title>${type} Reports</title>
                    <style>
                        table { border-collapse: collapse; width: 100%; margin: 20px 0; }
                        th, td { border: 1px solid #000; padding: 8px; text-align: left; }
                        h1 { color: #333; }
                    </style>
                </head>
                <body>
                    <h1>${type.charAt(0).toUpperCase() + type.slice(1)} Reports</h1>
                    ${filteredReports.length > 0 ? `
                        <table>
                            <thead>
                                <tr>
                                    <th>Reported By</th>
                                    <th>Link Reported</th>
                                    <th>Description</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${filteredReports.map(report => `
                                    <tr>
                                        <td>${report.users?.userusername || 'N/A'}</td>
                                        <td>${report.link_reported}</td>
                                        <td>${report.report_description}</td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    ` : '<p>No reports found.</p>'}
                </body>
            </html>
        `);
        printWindow.document.close();
        printWindow.print();
    };

    if (loading) {
        return <h1>Loading...</h1>;
    }

    return (
        <>
            <TopNav />
            <h1 className='reports-header'>Reports</h1>
            <div className="print-buttons">
                <button
                    className="print-button"
                    onClick={() => handlePrint('approved')}
                >
                    Print Approved Reports
                </button>
            </div>
            <table>
                <thead>
                    <tr>
                        <th>Reported By</th>
                        <th>Link Reported</th>
                        <th>Description</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {reports.map((report) => (
                        <tr key={report.report_id}>
                            <td>{report.users?.userusername || 'N/A'}</td>
                            <td>{report.link_reported}</td>
                            <td>{report.report_description}</td>
                            <td>
                                <button
                                    className="report-approve-button"
                                    onClick={() => handleApprove(report.report_id, report.link_reported)}
                                    disabled={report.approved} // Disable if approved
                                >
                                    {report.approved ? "Approved" : "Approve"}
                                </button>
                                <button className="report-delete-button" onClick={() => handleDelete(report.report_id)}>Delete</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </>
    );
};

export default Reports;
