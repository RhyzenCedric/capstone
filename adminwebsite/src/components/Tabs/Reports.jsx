import React, { useEffect, useState } from 'react';
import axios from 'axios';
import TopNav from "../NavBars/TopNav";

const Reports = () => {
    const [reports, setReports] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchReports();
    }, []);

    const fetchReports = async () => {
        try {
            const response = await axios.get('http://localhost:5000/reports');
            setReports(response.data);
        } catch (error) {
            console.error('Error fetching reports:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleApprove = async (report_id, link) => {
        if (window.confirm("Are you sure you want to approve this report?")) {
            try {
                const response = await axios.post('http://localhost:5000/reports/approve', { report_id, link });
                if (response.status === 200) {
                    alert("Report approved successfully!");
                    setReports(prevReports =>
                        prevReports.map(report =>
                            report.report_id === report_id ? { ...report, approved: true } : report
                        )
                    );
                }
            } catch (error) {
                console.error('Error approving report:', error);
            }
        }
    };

    const handleDelete = async (report_id) => {
        if (window.confirm("Are you sure you want to delete this report?")) {
            try {
                const response = await axios.delete(`http://localhost:5000/reports/${report_id}`);
                if (response.status === 200) {
                    setReports(prevReports => prevReports.filter(report => report.report_id !== report_id));
                } else {
                    throw new Error('Failed to delete report');
                }
            } catch (error) {
                console.error('Error deleting report:', error);
            }
        }
    };

    if (loading) {
        return <h1>Loading...</h1>;
    }

    return (
        <>
            <TopNav />
            <h1 className='reports-header'>Reports</h1>
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
                            <td>{report.userUsername}</td>
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
