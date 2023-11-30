import React from "react";
import { AdminPanelSettings } from "./AdminPanelSettings";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AdminPanelSettings",
  component: AdminPanelSettings,
};

export const Default = () => <AdminPanelSettings />;
export const Fill = () => <AdminPanelSettings fill="blue" />;
export const Size = () => <AdminPanelSettings height="50" width="50" />;
export const CustomStyle = () => <AdminPanelSettings style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AdminPanelSettings className="custom-class" />;
export const Clickable = () => <AdminPanelSettings onClick={()=>console.log("clicked")} />;

const Template = (args) => <AdminPanelSettings {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
