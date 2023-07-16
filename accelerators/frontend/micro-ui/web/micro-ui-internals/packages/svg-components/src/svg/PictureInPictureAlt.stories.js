import React from "react";
import { PictureInPictureAlt } from "./PictureInPictureAlt";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PictureInPictureAlt",
  component: PictureInPictureAlt,
};

export const Default = () => <PictureInPictureAlt />;
export const Fill = () => <PictureInPictureAlt fill="blue" />;
export const Size = () => <PictureInPictureAlt height="50" width="50" />;
export const CustomStyle = () => <PictureInPictureAlt style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PictureInPictureAlt className="custom-class" />;
