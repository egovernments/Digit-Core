import React from "react";
import { Plagiarism } from "./Plagiarism";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Plagiarism",
  component: Plagiarism,
};

export const Default = () => <Plagiarism />;
export const Fill = () => <Plagiarism fill="blue" />;
export const Size = () => <Plagiarism height="50" width="50" />;
export const CustomStyle = () => <Plagiarism style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Plagiarism className="custom-class" />;

export const Clickable = () => <Plagiarism onClick={()=>console.log("clicked")} />;

const Template = (args) => <Plagiarism {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
