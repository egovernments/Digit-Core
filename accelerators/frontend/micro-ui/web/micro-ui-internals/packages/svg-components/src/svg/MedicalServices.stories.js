import React from "react";
import { MedicalServices } from "./MedicalServices";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "MedicalServices",
  component: MedicalServices,
};

export const Default = () => <MedicalServices />;
export const Fill = () => <MedicalServices fill="blue" />;
export const Size = () => <MedicalServices height="50" width="50" />;
export const CustomStyle = () => <MedicalServices style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <MedicalServices className="custom-class" />;

export const Clickable = () => <MedicalServices onClick={()=>console.log("clicked")} />;

const Template = (args) => <MedicalServices {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
