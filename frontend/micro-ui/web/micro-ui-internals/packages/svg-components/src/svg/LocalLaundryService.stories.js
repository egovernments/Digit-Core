import React from "react";
import { LocalLaundryService } from "./LocalLaundryService";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalLaundryService",
  component: LocalLaundryService,
};

export const Default = () => <LocalLaundryService />;
export const Fill = () => <LocalLaundryService fill="blue" />;
export const Size = () => <LocalLaundryService height="50" width="50" />;
export const CustomStyle = () => <LocalLaundryService style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalLaundryService className="custom-class" />;

export const Clickable = () => <LocalLaundryService onClick={()=>console.log("clicked")} />;

const Template = (args) => <LocalLaundryService {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
