import React from "react";
import { PictureInPicture } from "./PictureInPicture";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PictureInPicture",
  component: PictureInPicture,
};

export const Default = () => <PictureInPicture />;
export const Fill = () => <PictureInPicture fill="blue" />;
export const Size = () => <PictureInPicture height="50" width="50" />;
export const CustomStyle = () => <PictureInPicture style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PictureInPicture className="custom-class" />;

export const Clickable = () => <PictureInPicture onClick={()=>console.log("clicked")} />;

const Template = (args) => <PictureInPicture {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
