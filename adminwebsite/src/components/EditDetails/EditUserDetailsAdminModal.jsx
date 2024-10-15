// Modal.js
import React from 'react';
import '../../css/EditUserDetailsAdminModal.css'; // You can add your CSS for the modal here

const Modal = ({ children, onClose }) => {
    return (
        <div className="modal-overlay">
            <div className="modal-content">
                <button className="modal-close" onClick={onClose}>
                    &times;
                </button>
                {children}
            </div>
        </div>
    );
};

export default Modal;
